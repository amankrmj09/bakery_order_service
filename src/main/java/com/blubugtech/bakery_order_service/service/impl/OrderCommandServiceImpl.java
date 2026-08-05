package com.blubugtech.bakery_order_service.service.impl;

import com.blubugtech.bakery_order_service.dto.item.OrderItemRequest;
import com.blubugtech.bakery_order_service.dto.order.OrderRequest;
import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.dto.order.OrderStatusUpdateRequest;
import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.entity.OrderItem;
import com.blubugtech.bakery_order_service.enums.OrderStatus;

import com.blubugtech.bakery_order_service.exception.OrderServiceException;
import com.blubugtech.bakery_order_service.exception.ProductNotFoundException;
import com.blubugtech.bakery_order_service.exception.InsufficientStockException;
import com.blubugtech.bakery_order_service.gateway.ProductGateway;
import com.blubugtech.bakery_order_service.inventory.InventoryService;
import com.blubugtech.bakery_order_service.mapper.OrderMapper;
import com.blubugtech.bakery_order_service.pricing.OrderPricingService;
import com.blubugtech.bakery_order_service.repository.OrderRepository;
import com.blubugtech.bakery_order_service.service.OrderCommandService;
import com.blubugtech.bakery_order_service.validation.OrderStatusValidator;
import com.blubugtech.bakery_order_service.validation.OrderValidator;
import org.blubakery.common.feign.contract.feign.Product;
import org.blubakery.common.feign.contract.feign.StockAvailability;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_order_service.event.OrderEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderRepository orderRepository;
    private final ProductGateway productGateway;
    private final OrderPricingService orderPricingService;
    private final InventoryService inventoryService;
    private final OrderValidator orderValidator;
    private final OrderStatusValidator orderStatusValidator;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;
    private final com.blubugtech.bakery_order_service.gateway.StatisticsGateway statisticsGateway;

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
            order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
            order.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
            if (request.getPaymentMethod() != null) {
                order.setPaymentMethod(request.getPaymentMethod());
            }
            order.setPaymentStatus("PENDING");

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

            orderEventPublisher.publishOrderCreated(savedOrder, null);

            return orderMapper.toResponse(savedOrder);

        } catch (Exception e) {
            log.error("Failed to create order for user: {}", request.getUserId(), e);
            throw new OrderServiceException("Failed to create order", e);
        }
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));

        orderStatusValidator.validateStatusTransition(order.getStatus(), request.getStatus());

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        if (request.getStatus() == OrderStatus.CONFIRMED) {
            order.setConfirmedAt(LocalDateTime.now());
            inventoryService.consumeStockForOrder(order);
        } else if (request.getStatus() == OrderStatus.DELIVERED) {
            order.setCompletedAt(LocalDateTime.now());
            try {
                statisticsGateway.addRevenue(new org.blubakery.common.messaging.revenue.RevenuePayload(order.getTotalAmount()));
            } catch (Exception e) {
                log.error("Failed to update central dashboard revenue", e);
            }
        } else if (request.getStatus() == OrderStatus.CANCELLED) {
            order.setCancelledAt(LocalDateTime.now());
            order.setCancellationReason(request.getNotes());
        }

        handleStatusTransition(oldStatus, request.getStatus());

        Order updatedOrder = orderRepository.save(order);
        
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        if (request.getStatus() == OrderStatus.CANCELLED) {
            metadata.put("cancelledByAdmin", true);
        }

        orderEventPublisher.publishOrderStatusUpdated(updatedOrder, oldStatus, request.getStatus(), metadata);
        
        if (request.getStatus() == OrderStatus.CONFIRMED) {
            String invoiceUrl = "https://shahs-bakery.com/invoices/INV-" + updatedOrder.getOrderNumber() + ".pdf";
            orderEventPublisher.publishInvoiceGenerated(updatedOrder, invoiceUrl);
        }

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    public OrderResponse updatePaymentStatus(UUID orderId, String paymentStatus, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));

        order.setPaymentStatus(paymentStatus);
        OrderStatus oldStatus = order.getStatus();
        
        if ("COMPLETED".equalsIgnoreCase(paymentStatus) || "SUCCESS".equalsIgnoreCase(paymentStatus) || "PAID".equalsIgnoreCase(paymentStatus)) {
            if ("CASH".equalsIgnoreCase(order.getPaymentMethod()) || "COD".equalsIgnoreCase(order.getPaymentMethod())) {
                order.setPaymentMethod("CARD");
            }
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
                order.setConfirmedAt(LocalDateTime.now());
                inventoryService.consumeStockForOrder(order);
                handleStatusTransition(oldStatus, OrderStatus.CONFIRMED);
                orderEventPublisher.publishOrderStatusUpdated(order, oldStatus, OrderStatus.CONFIRMED, null);
                
                String invoiceUrl = "https://shahs-bakery.com/invoices/INV-" + order.getOrderNumber() + ".pdf";
                orderEventPublisher.publishInvoiceGenerated(order, invoiceUrl);
            }
        }
        Order updatedOrder = orderRepository.save(order);
        orderEventPublisher.publishPaymentStatusUpdated(updatedOrder, null);
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    public OrderResponse cancelOrder(UUID orderId, String reason, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));

        if (!order.canBeCancelled(isAdmin)) {
            throw new OrderServiceException("Order cannot be cancelled in current status or payment state: " + order.getStatus() + " (payment: " + order.getPaymentStatus() + ")");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);

        handleStatusTransition(oldStatus, OrderStatus.CANCELLED);

        inventoryService.releaseStockForOrder(order);

        Order cancelledOrder = orderRepository.save(order);
        
        orderEventPublisher.publishOrderStatusUpdated(cancelledOrder, oldStatus, OrderStatus.CANCELLED, java.util.Map.of("cancelledByAdmin", isAdmin));

        return orderMapper.toResponse(cancelledOrder);
    }

    @Override
    public OrderResponse cancelOrder(UUID orderId, String reason) {
        return cancelOrder(orderId, reason, false);
    }

    private OrderItem createOrderItem(Order order, OrderItemRequest itemRequest) {
        Product productResponse;
        try {
            productResponse = productGateway.getProductById(itemRequest.getProductId());
        } catch (Exception e) {
            throw new ProductNotFoundException("Product not found: " + itemRequest.getProductId(), e);
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

    private void handleStatusTransition(OrderStatus oldStatus, OrderStatus newStatus) {
        boolean wasActive = (oldStatus == OrderStatus.CONFIRMED || oldStatus == OrderStatus.PREPARING || 
                             oldStatus == OrderStatus.READY || oldStatus == OrderStatus.OUT_FOR_DELIVERY);
        boolean isNowActive = (newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.PREPARING || 
                               newStatus == OrderStatus.READY || newStatus == OrderStatus.OUT_FOR_DELIVERY);

        if (!wasActive && isNowActive) {
            statisticsGateway.incrementOrders();
        } else if (wasActive && !isNowActive) {
            statisticsGateway.decrementOrders();
        }
    }
}
