package com.shah_s.bakery_order_service.service;

import com.shah_s.bakery_order_service.client.ProductServiceClient;
import com.shah_s.bakery_order_service.client.PaymentServiceClient;
import com.shah_s.bakery_order_service.client.NotificationServiceClient;
import com.shah_s.bakery_order_service.client.InternalStatsClient;
import com.shah_s.bakery_order_service.dto.*;
import com.shah_s.bakery_order_service.entity.Order;
import com.shah_s.bakery_order_service.entity.OrderItem;
import com.shah_s.bakery_order_service.exception.OrderServiceException;
import com.shah_s.bakery_order_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.shah_s.bakery_order_service.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);


    final private OrderRepository orderRepository;

    final private ProductServiceClient productServiceClient;

    final private PaymentServiceClient paymentServiceClient;
    
    final private NotificationServiceClient notificationServiceClient;
    
    final private InternalStatsClient internalStatsClient;
    
    final private OrderEventPublisher orderEventPublisher;

    @Value("${order.tax.rate:0.08}")
    private BigDecimal taxRate;

    @Value("${order.delivery.default-time-minutes:60}")
    private Integer defaultDeliveryTimeMinutes;

    @Value("${order.limits.max-items-per-order:50}")
    private Integer maxItemsPerOrder;

    @Value("${order.limits.max-order-value:500.00}")
    private BigDecimal maxOrderValue;

    public OrderService(OrderRepository orderRepository, ProductServiceClient productServiceClient, PaymentServiceClient paymentServiceClient, NotificationServiceClient notificationServiceClient, InternalStatsClient internalStatsClient, OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
        this.paymentServiceClient = paymentServiceClient;
        this.notificationServiceClient = notificationServiceClient;
        this.internalStatsClient = internalStatsClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    // Create new order
    public OrderResponse createOrder(OrderRequest request) {
        logger.info("Creating new order for user: {}", request.getUserId());

        try {
            // Validate order request
            validateOrderRequest(request);

            // Create order entity
            Order order = new Order(request.getUserId(), request.getCustomerName(),
                    request.getCustomerEmail(), request.getDeliveryType());
            order.setCustomerPhone(request.getCustomerPhone());
            order.setDeliveryAddress(request.getDeliveryAddress());
            order.setDeliveryDate(request.getDeliveryDate());
            order.setSpecialInstructions(request.getSpecialInstructions());
            order.setDiscountCode(request.getDiscountCode());

            // Process order items
            for (OrderItemRequest itemRequest : request.getItems()) {
                OrderItem orderItem = createOrderItem(order, itemRequest);
                order.addOrderItem(orderItem);
            }

            // Calculate preparation time
            calculatePreparationTime(order);

            // Apply discounts if any
            applyDiscounts(order, request.getDiscountCode());

            // Calculate totals
            order.calculateTotals(taxRate);

            // Set delivery fee
            setDeliveryFee(order);

            // Recalculate totals with delivery fee
            order.calculateTotals(taxRate);

            // Validate payment amount matches order total
            if (request.getPaymentAmount() == null || request.getPaymentAmount().compareTo(order.getTotalAmount()) < 0) {
                logger.error("Payment amount mismatch: provided {} but order total is {}", request.getPaymentAmount(), order.getTotalAmount());
                throw new OrderServiceException("Payment amount does not match order total. Please provide the correct amount: " + order.getTotalAmount());
            }

            // Validate order limits
            validateOrderLimits(order);

            // Reserve stock for all items
            reserveStockForOrder(order);

            // ✅ Save order FIRST (without payment)
            Order savedOrder = orderRepository.save(order);

            // ✅ Create payment through Payment Service
            Map<String, Object> paymentRequest = new java.util.HashMap<>();
            paymentRequest.put("orderId", savedOrder.getId());
            paymentRequest.put("userId", savedOrder.getUserId());
            paymentRequest.put("paymentMethod", request.getPaymentMethod());
            paymentRequest.put("amount", savedOrder.getTotalAmount());
            paymentRequest.put("currencyCode", request.getCurrencyCode());
            paymentRequest.put("description", "Payment for order " + savedOrder.getOrderNumber());
            paymentRequest.put("cardLastFour", request.getCardLastFour());
            paymentRequest.put("cardBrand", request.getCardBrand());
            paymentRequest.put("cardType", request.getCardType());
            paymentRequest.put("digitalWalletProvider", request.getDigitalWalletProvider());
            paymentRequest.put("bankName", request.getBankName());
            paymentRequest.put("notes", request.getPaymentNotes());

            try {
                Map<String, Object> paymentResponse = paymentServiceClient.createPayment(paymentRequest);
                logger.info("Payment created for order: {} - Payment ID: {}",
                        savedOrder.getOrderNumber(), paymentResponse.get("id"));
            } catch (Exception e) {
                logger.error("Failed to create payment for order {}: {}", savedOrder.getOrderNumber(), e.getMessage());
                // Don't fail the order creation, payment can be retried
            }

            logger.info("Order created successfully: {} (Order Number: {})",
                    savedOrder.getId(), savedOrder.getOrderNumber());
                    
            // Publish Kafka Event
            try {
                org.devofblue.common.event.OrderEvent event = org.devofblue.common.event.OrderEvent.builder()
                        .orderId(savedOrder.getId())
                        .orderNumber(savedOrder.getOrderNumber())
                        .userId(savedOrder.getUserId())
                        .customerEmail(savedOrder.getCustomerEmail())
                        .totalAmount(savedOrder.getTotalAmount())
                        .status(savedOrder.getStatus().name())
                        .timestamp(LocalDateTime.now())
                        .build();
                orderEventPublisher.publishOrderCreated(event);
            } catch (Exception ex) {
                logger.error("Failed to publish OrderCreated event for {}: {}", savedOrder.getId(), ex.getMessage());
            }
                    
            // Notification will be handled asynchronously via Kafka OrderEvent

            return OrderResponse.from(savedOrder);

        } catch (Exception e) {
            logger.error("Failed to create order for user: {} - {}", request.getUserId(), e.getMessage());
            // Release any reserved stock
            releaseStockForFailedOrder(request);
            throw new OrderServiceException("Failed to create order: " + e.getMessage());
        }
    }

    // Get order by ID
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        logger.debug("Fetching order by ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));

        return OrderResponse.from(order);
    }

    // Get order by order number
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderByOrderNumber(String orderNumber) {
        logger.debug("Fetching order by order number: {}", orderNumber);

        return orderRepository.findByOrderNumber(orderNumber)
                .map(OrderResponse::from);
    }

    // Get orders by user ID
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        logger.debug("Fetching orders for user: {}", userId);

        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // Get orders by user ID with pagination
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserIdWithPagination(UUID userId, Pageable pageable) {
        logger.debug("Fetching orders for user with pagination: {}", userId);

        return orderRepository.findByUserId(userId, pageable)
                .map(OrderResponse::from);
    }

    // Get orders by status
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(Order.OrderStatus status) {
        logger.debug("Fetching orders by status: {}", status);

        return orderRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // Get all orders with pagination
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        logger.debug("Fetching all orders with pagination");

        return orderRepository.findAll(pageable)
                .map(OrderResponse::from);
    }

    // Update order status
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request) {
        logger.info("Updating order status: {} to {}", orderId, request.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), request.getStatus());

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        // Handle status-specific logic
        handleStatusTransition(order, oldStatus, request.getStatus(), request.getReason());

        Order updatedOrder = orderRepository.save(order);
        logger.info("Order status updated successfully: {} from {} to {}",
                orderId, oldStatus, request.getStatus());
                
        // Publish Kafka Event
        try {
            org.devofblue.common.event.OrderEvent event = org.devofblue.common.event.OrderEvent.builder()
                    .orderId(updatedOrder.getId())
                    .orderNumber(updatedOrder.getOrderNumber())
                    .userId(updatedOrder.getUserId())
                    .customerEmail(updatedOrder.getCustomerEmail())
                    .totalAmount(updatedOrder.getTotalAmount())
                    .status(updatedOrder.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .build();
            orderEventPublisher.publishOrderStatusUpdated(event);
        } catch (Exception ex) {
            logger.error("Failed to publish OrderStatusUpdated event for {}: {}", updatedOrder.getId(), ex.getMessage());
        }
                
        // Central Dashboard Statistics Updates
        try {
            if (oldStatus != Order.OrderStatus.CONFIRMED && request.getStatus() == Order.OrderStatus.CONFIRMED) {
                internalStatsClient.incrementOrders();
            } else if ((oldStatus != Order.OrderStatus.DELIVERED && oldStatus != Order.OrderStatus.CANCELLED) && 
                       (request.getStatus() == Order.OrderStatus.DELIVERED || request.getStatus() == Order.OrderStatus.CANCELLED)) {
                // Wait, if it wasn't confirmed yet and is cancelled, do we decrement? 
                // Only if it was previously confirmed, preparing, ready, out for delivery
                if (oldStatus == Order.OrderStatus.CONFIRMED || oldStatus == Order.OrderStatus.PREPARING || 
                    oldStatus == Order.OrderStatus.READY || oldStatus == Order.OrderStatus.OUT_FOR_DELIVERY) {
                    internalStatsClient.decrementOrders();
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to update central dashboard statistics for order {}: {}", orderId, ex.getMessage());
        }
                
        // Notification will be handled asynchronously via Kafka OrderEvent

        return OrderResponse.from(updatedOrder);
    }

    // Cancel order
    public OrderResponse cancelOrder(UUID orderId, String reason) {
        logger.info("Cancelling order: {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));

        if (!order.canBeCancelled()) {
            throw new OrderServiceException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);

        // Release reserved stock
        releaseStockForOrder(order);

        // ✅ Cancel payment through Payment Service (if payment exists)
        try {
            Map<String, Object> paymentResponse = paymentServiceClient.getPaymentByOrderId(orderId);
            if (paymentResponse != null) {
                String paymentId = (String) paymentResponse.get("id");
                Map<String, String> cancelRequest = Map.of("reason", reason);
                paymentServiceClient.cancelPayment(UUID.fromString(paymentId), cancelRequest);
                logger.info("Payment cancelled for order: {}", orderId);
            }
        } catch (Exception e) {
            logger.error("Failed to cancel payment for order {}: {}", orderId, e.getMessage());
            // Don't fail order cancellation if payment cancellation fails
        }

        Order cancelledOrder = orderRepository.save(order);
        logger.info("Order cancelled successfully: {}", orderId);
        
        // Publish Kafka Event
        try {
            org.devofblue.common.event.OrderEvent event = org.devofblue.common.event.OrderEvent.builder()
                    .orderId(cancelledOrder.getId())
                    .orderNumber(cancelledOrder.getOrderNumber())
                    .userId(cancelledOrder.getUserId())
                    .customerEmail(cancelledOrder.getCustomerEmail())
                    .totalAmount(cancelledOrder.getTotalAmount())
                    .status(cancelledOrder.getStatus().name())
                    .timestamp(LocalDateTime.now())
                    .build();
            orderEventPublisher.publishOrderStatusUpdated(event);
        } catch (Exception ex) {
            logger.error("Failed to publish OrderStatusUpdated (Cancel) event for {}: {}", cancelledOrder.getId(), ex.getMessage());
        }

        return OrderResponse.from(cancelledOrder);
    }

    // Get recent orders
    @Transactional(readOnly = true)
    public List<OrderResponse> getRecentOrders(int days) {
        logger.debug("Fetching orders from last {} days", days);

        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return orderRepository.findRecentOrders(sinceDate).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // Search orders
    @Transactional(readOnly = true)
    public List<OrderResponse> searchOrders(String searchTerm) {
        logger.debug("Searching orders with term: {}", searchTerm);

        return orderRepository.searchByCustomerInfo(searchTerm).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // ✅ FIXED: Remove Payment.PaymentMethod reference
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersWithFilters(UUID userId, Order.OrderStatus status,
                                                    Order.DeliveryType deliveryType,
                                                    String paymentMethod, // Changed to String
                                                    BigDecimal minAmount, BigDecimal maxAmount,
                                                    LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching orders with filters");

        // For now, ignore paymentMethod filter since we don't have Payment entity in Order Service
        return orderRepository.findOrdersWithFilters(userId, status, deliveryType, null,
                        minAmount, maxAmount, startDate, endDate).stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    // Get order statistics
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching order statistics");

        try {
            // Simple counts and totals
            long totalOrders = orderRepository.countByCreatedAtBetween(startDate, endDate);
            long pendingOrders = orderRepository.countByStatusAndCreatedAtBetween(Order.OrderStatus.PENDING, startDate, endDate);
            long completedOrders = orderRepository.countByStatusAndCreatedAtBetween(Order.OrderStatus.DELIVERED, startDate, endDate);
            long cancelledOrders = orderRepository.countByStatusAndCreatedAtBetween(Order.OrderStatus.CANCELLED, startDate, endDate);

            // Get orders for calculation
            List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);

            BigDecimal totalRevenue = orders.stream()
                    .filter(o -> o.getStatus() != Order.OrderStatus.CANCELLED)
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
                    "dateRange", Map.of(
                            "startDate", startDate.toString(),
                            "endDate", endDate.toString()
                    )
            );

        } catch (Exception e) {
            logger.error("Error fetching order statistics: {}", e.getMessage());
            return Map.of(
                    "error", "Statistics temporarily unavailable",
                    "message", e.getMessage()
            );
        }
    }

    // Private helper methods
    private void validateOrderRequest(OrderRequest request) {
        if (request.getItems().size() > maxItemsPerOrder) {
            throw new OrderServiceException("Order cannot contain more than " + maxItemsPerOrder + " items");
        }

        if (request.getDeliveryType() == Order.DeliveryType.DELIVERY &&
                (request.getDeliveryAddress() == null || request.getDeliveryAddress().trim().isEmpty())) {
            throw new OrderServiceException("Delivery address is required for delivery orders");
        }
    }

    private OrderItem createOrderItem(Order order, OrderItemRequest itemRequest) {
        // Get product details from Product Service
        Map<String, Object> productResponse;
        try {
            productResponse = productServiceClient.getProductById(itemRequest.getProductId());
        } catch (Exception e) {
            throw new ProductNotFoundException("Product not found: " + itemRequest.getProductId());
        }

        // Check stock availability
        Map<String, Object> stockResponse = productServiceClient.checkStockAvailability(
                itemRequest.getProductId(), itemRequest.getQuantity());

        Boolean sufficient = (Boolean) stockResponse.get("sufficient");
        if (!sufficient) {
            String productName = (String) productResponse.get("name");
            throw new InsufficientStockException("Insufficient stock for product: " + productName);
        }

        // Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductId(itemRequest.getProductId());
        orderItem.setProductSku((String) productResponse.get("sku"));
        orderItem.setProductName((String) productResponse.get("name"));
        orderItem.setProductCategory(getProductCategory(productResponse));
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setUnitPrice(getProductPrice(productResponse, itemRequest.getUnitPriceOverride()));
        orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
        orderItem.setProductDescription((String) productResponse.get("description"));
        orderItem.setProductImageUrl(getProductImageUrl(productResponse));
        orderItem.setPreparationTimeMinutes(getProductPreparationTime(productResponse));

        return orderItem;
    }

    private void calculatePreparationTime(Order order) {
        int totalPreparationMinutes = order.getOrderItems().stream()
                .mapToInt(OrderItem::getTotalPreparationTime)
                .max()
                .orElse(defaultDeliveryTimeMinutes);

        order.setEstimatedPreparationMinutes(totalPreparationMinutes);
        order.setEstimatedReadyTime(LocalDateTime.now().plusMinutes(totalPreparationMinutes));
    }

    private void applyDiscounts(Order order, String discountCode) {
        // Implement discount logic here
        // For now, just placeholder
        if (discountCode != null && !discountCode.trim().isEmpty()) {
            // Apply discount based on code
            // This would typically involve checking a discount table
            logger.info("Applying discount code: {}", discountCode);
        }
    }

    private void setDeliveryFee(Order order) {
        if (order.getDeliveryType() == Order.DeliveryType.DELIVERY) {
            // Set delivery fee based on business rules
            order.setDeliveryFee(new BigDecimal("5.00")); // Fixed delivery fee for now
        } else {
            order.setDeliveryFee(BigDecimal.ZERO);
        }
    }

    private void validateOrderLimits(Order order) {
        if (order.getTotalAmount().compareTo(maxOrderValue) > 0) {
            throw new OrderServiceException("Order value exceeds maximum limit of $" + maxOrderValue);
        }
    }

    private void reserveStockForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            try {
                Map<String, Integer> request = Map.of("quantity", item.getQuantity());
                Map<String, Object> response = productServiceClient.reserveStock(item.getProductId(), request);
                Boolean success = (Boolean) response.get("success");

                if (!success) {
                    throw new OrderServiceException("Failed to reserve stock for product: " + item.getProductName());
                }
            } catch (Exception e) {
                throw new OrderServiceException("Stock reservation failed: " + e.getMessage());
            }
        }
    }

    private void releaseStockForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            try {
                Map<String, Integer> request = Map.of("quantity", item.getQuantity());
                productServiceClient.releaseReservedStock(item.getProductId(), request);
            } catch (Exception e) {
                logger.error("Failed to release stock for product {}: {}", item.getProductId(), e.getMessage());
            }
        }
    }

    private void releaseStockForFailedOrder(OrderRequest request) {
        for (OrderItemRequest item : request.getItems()) {
            try {
                Map<String, Integer> releaseRequest = Map.of("quantity", item.getQuantity());
                productServiceClient.releaseReservedStock(item.getProductId(), releaseRequest);
            } catch (Exception e) {
                logger.error("Failed to release stock for failed order, product {}: {}",
                        item.getProductId(), e.getMessage());
            }
        }
    }

    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        boolean isValidTransition = false;
        if (currentStatus == Order.OrderStatus.DELIVERED || currentStatus == Order.OrderStatus.CANCELLED) {
            isValidTransition = false; // Terminal states
        } else if (newStatus == Order.OrderStatus.CANCELLED) {
            isValidTransition = true; // Can cancel anytime before delivery
        } else {
            // Allow skipping forward (e.g. CONFIRMED directly to DELIVERED)
            isValidTransition = newStatus.ordinal() > currentStatus.ordinal();
        }

        if (!isValidTransition) {
            throw new InvalidOrderStatusException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void handleStatusTransition(Order order, Order.OrderStatus oldStatus,
                                        Order.OrderStatus newStatus, String reason) {
        LocalDateTime now = LocalDateTime.now();

        switch (newStatus) {
            case CONFIRMED -> {
                order.setConfirmedAt(now);
                // Consume reserved stock
                consumeStockForOrder(order);
            }
            case DELIVERED -> {
                order.setCompletedAt(now);
                // Add revenue to central dashboard statistics
                try {
                    Map<String, Object> payload = new java.util.HashMap<>();
                    payload.put("amount", order.getTotalAmount());
                    internalStatsClient.addRevenue(payload);
                } catch (Exception ex) {
                    logger.error("Failed to update central dashboard revenue for order {}: {}", order.getId(), ex.getMessage());
                }
            }
            case CANCELLED -> {
                order.setCancelledAt(now);
                order.setCancellationReason(reason);
                // Stock release handled in cancelOrder method
            }
        }
    }

    private void consumeStockForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            try {
                Map<String, Integer> request = Map.of("quantity", item.getQuantity());
                productServiceClient.consumeStock(item.getProductId(), request);
            } catch (Exception e) {
                logger.error("Failed to consume stock for product {}: {}", item.getProductId(), e.getMessage());
                throw new OrderServiceException("Stock consumption failed: " + e.getMessage());
            }
        }
    }

    // Utility methods for product response parsing
    private String getProductCategory(Map<String, Object> productResponse) {
        Object categoryObj = productResponse.get("category");
        if (categoryObj instanceof Map<?, ?> categoryMap) {
            Object nameObj = categoryMap.get("name");
            return nameObj != null ? nameObj.toString() : null;
        }
        return null;
    }

    private BigDecimal getProductPrice(Map<String, Object> productResponse, BigDecimal priceOverride) {
        if (priceOverride != null) {
            return priceOverride;
        }

        Object effectivePrice = productResponse.get("effectivePrice");
        if (effectivePrice instanceof Number) {
            return BigDecimal.valueOf(((Number) effectivePrice).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private String getProductImageUrl(Map<String, Object> productResponse) {
        return (String) productResponse.get("primaryImageUrl");
    }

    private Integer getProductPreparationTime(Map<String, Object> productResponse) {
        Object prepTime = productResponse.get("preparationTimeMinutes");
        return prepTime instanceof Number ? ((Number) prepTime).intValue() : 30; // Default 30 minutes
    }
}
