package com.blubugtech.bakery_order_service.service.impl;

import com.blubugtech.bakery_order_service.dto.item.OrderItemRequest;
import com.blubugtech.bakery_order_service.dto.order.OrderRequest;
import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.dto.order.OrderStatusUpdateRequest;
import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.entity.OrderItem;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import com.blubugtech.bakery_order_service.exception.OrderServiceException;
import com.blubugtech.bakery_order_service.exception.ProductNotFoundException;
import com.blubugtech.bakery_order_service.exception.InsufficientStockException;
import com.blubugtech.bakery_order_service.gateway.ProductGateway;
import com.blubugtech.bakery_order_service.gateway.StatisticsGateway;
import com.blubugtech.bakery_order_service.integration.event.OrderEventDispatcher;
import com.blubugtech.bakery_order_service.inventory.InventoryService;
import com.blubugtech.bakery_order_service.mapper.OrderMapper;
import com.blubugtech.bakery_order_service.pricing.OrderPricingService;
import com.blubugtech.bakery_order_service.repository.OrderRepository;
import com.blubugtech.bakery_order_service.service.OrderService;
import com.blubugtech.bakery_order_service.validation.OrderStatusValidator;
import com.blubugtech.bakery_order_service.validation.OrderValidator;
import com.blubugtech.common.contract.feign.Product;
import com.blubugtech.common.contract.feign.StockAvailability;
import com.blubugtech.common.contract.messaging.OrderPayload;
import com.blubugtech.common.contract.messaging.RevenuePayload;
import com.blubugtech.common.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductGateway productGateway;
    private final StatisticsGateway statisticsGateway;
    private final OrderEventDispatcher orderEventDispatcher;
    private final OrderPricingService orderPricingService;
    private final InventoryService inventoryService;
    private final OrderValidator orderValidator;
    private final OrderStatusValidator orderStatusValidator;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating new order for user: {}", request.getUserId());

        try {
            orderValidator.validateOrderRequest(request);

            Order order = new Order(request.getUserId(), request.getCustomerName(),
                    request.getCustomerEmail(), request.getDeliveryType());
            order.setCustomerPhone(request.getCustomerPhone());
            order.setDeliveryAddress(request.getDeliveryAddress());
            order.setDeliveryDate(request.getDeliveryDate());
            order.setSpecialInstructions(request.getSpecialInstructions());
            order.setDiscountCode(request.getDiscountCode());

            for (OrderItemRequest itemRequest : request.getItems()) {
                OrderItem orderItem = createOrderItem(order, itemRequest);
                order.addOrderItem(orderItem);
            }

            orderPricingService.applyPricingAndTiming(order, request.getDiscountCode());

            orderValidator.validatePaymentAmount(request, order);
            orderValidator.validateOrderLimits(order);

            inventoryService.reserveStockForOrder(order);

            Order savedOrder = orderRepository.save(order);

            log.info("Order created successfully: {} (Order Number: {})",
                    savedOrder.getId(), savedOrder.getOrderNumber());

            publishOrderEvent(savedOrder, "CREATED");

            return orderMapper.toResponse(savedOrder);

        } catch (Exception e) {
            log.error("Failed to create order for user: {} - {}", request.getUserId(), e.getMessage());
            // In a real scenario, compensating transactions for stock would be needed if something failed after reservation
            throw new OrderServiceException("Failed to create order: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserIdWithPagination(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));

        orderStatusValidator.validateStatusTransition(order.getStatus(), request.getStatus());

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        handleStatusTransition(order, oldStatus, request.getStatus(), request.getNotes());

        Order updatedOrder = orderRepository.save(order);
        publishOrderEvent(updatedOrder, "STATUS_UPDATED");

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    public OrderResponse cancelOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));

        if (!order.canBeCancelled()) {
            throw new OrderServiceException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);

        inventoryService.releaseStockForOrder(order);

        Order cancelledOrder = orderRepository.save(order);
        publishOrderEvent(cancelledOrder, "CANCELLED");

        return orderMapper.toResponse(cancelledOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getRecentOrders(int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return orderRepository.findRecentOrders(sinceDate).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> searchOrders(String searchTerm) {
        return orderRepository.searchByCustomerInfo(searchTerm).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersWithFilters(UUID userId, OrderStatus status, DeliveryType deliveryType, String paymentMethod, BigDecimal minAmount, BigDecimal maxAmount, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersWithFilters(userId, status, deliveryType, null, minAmount, maxAmount, startDate, endDate).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        long totalOrders = orderRepository.countByCreatedAtBetween(startDate, endDate);
        long pendingOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.PENDING, startDate, endDate);
        long completedOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, startDate, endDate);
        long cancelledOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.CANCELLED, startDate, endDate);

        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return Map.of(
                "totalOrders", totalOrders,
                "totalRevenue", totalRevenue,
                "averageOrderValue", averageOrderValue,
                "pendingOrders", pendingOrders,
                "completedOrders", completedOrders,
                "cancelledOrders", cancelledOrders,
                "dateRange", Map.of("startDate", startDate.toString(), "endDate", endDate.toString())
        );
    }

    private void handleStatusTransition(Order order, OrderStatus oldStatus, OrderStatus newStatus, String reason) {
        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case CONFIRMED -> {
                order.setConfirmedAt(now);
                inventoryService.consumeStockForOrder(order);
                statisticsGateway.incrementOrders();
                
                // Generate and publish invoice event
                String invoiceUrl = "https://shahs-bakery.com/invoices/INV-" + order.getOrderNumber() + ".pdf";
                OrderPayload invoicePayload = OrderPayload.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .userId(order.getUserId())
                        .customerEmail(order.getCustomerEmail())
                        .totalAmount(order.getTotalAmount())
                        .status("INVOICE_GENERATED")
                        .invoiceUrl(invoiceUrl)
                        .deliveryAddress(order.getDeliveryAddress())
                        .timestamp(LocalDateTime.now())
                        .build();
                OrderEvent invoiceEvent = OrderEvent.builder()
                        .eventType("INVOICE_GENERATED")
                        .payload(invoicePayload)
                        .build();
                orderEventDispatcher.dispatchOrderStatusUpdated(invoiceEvent);
            }
            case DELIVERED -> {
                order.setCompletedAt(now);
                try {
                    statisticsGateway.addRevenue(new RevenuePayload(order.getTotalAmount()));
                } catch (Exception e) {
                    log.error("Failed to update central dashboard revenue", e);
                }
            }
            case CANCELLED -> {
                order.setCancelledAt(now);
                order.setCancellationReason(reason);
                if (oldStatus == OrderStatus.CONFIRMED || oldStatus == OrderStatus.PREPARING || oldStatus == OrderStatus.READY || oldStatus == OrderStatus.OUT_FOR_DELIVERY) {
                    statisticsGateway.decrementOrders();
                }
            }
        }
    }

    private OrderItem createOrderItem(Order order, OrderItemRequest itemRequest) {
        Product productResponse;
        try {
            productResponse = productGateway.getProductById(itemRequest.getProductId());
        } catch (Exception e) {
            throw new ProductNotFoundException("Product not found: " + itemRequest.getProductId());
        }

        StockAvailability stockResponse = productGateway.checkStockAvailability(itemRequest.getProductId(), itemRequest.getQuantity());
        if (stockResponse.getSufficient() == null || !stockResponse.getSufficient()) {
            throw new InsufficientStockException("Insufficient stock for product: " + productResponse.getName());
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductId(itemRequest.getProductId());
        orderItem.setProductSku(productResponse.getSku());
        orderItem.setProductName(productResponse.getName());
        orderItem.setProductCategory(productResponse.getCategory() != null ? productResponse.getCategory().getName() : null);
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setUnitPrice(itemRequest.getUnitPriceOverride() != null ? itemRequest.getUnitPriceOverride() : productResponse.getEffectivePrice());
        orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
        orderItem.setProductDescription(productResponse.getDescription());
        orderItem.setProductImageUrl(productResponse.getPrimaryImageUrl());
        orderItem.setPreparationTimeMinutes(productResponse.getPreparationTimeMinutes() != null ? productResponse.getPreparationTimeMinutes() : 30);

        return orderItem;
    }

    private void publishOrderEvent(Order order, String eventType) {
        try {
            OrderPayload payload = OrderPayload.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userId(order.getUserId())
                    .customerEmail(order.getCustomerEmail())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus().name())
                    .deliveryAddress(order.getDeliveryAddress())
                    .timestamp(LocalDateTime.now())
                    .build();
            OrderEvent event = OrderEvent.builder()
                    .eventType(eventType.equals("CREATED") ? "ORDER_CREATED" : "ORDER_STATUS_UPDATED")
                    .payload(payload)
                    .build();
            if ("CREATED".equals(eventType)) {
                orderEventDispatcher.dispatchOrderCreated(event);
            } else {
                orderEventDispatcher.dispatchOrderStatusUpdated(event);
            }
        } catch (Exception ex) {
            log.error("Failed to publish Order Event ({}) for {}: {}", eventType, order.getId(), ex.getMessage());
        }
    }
}
