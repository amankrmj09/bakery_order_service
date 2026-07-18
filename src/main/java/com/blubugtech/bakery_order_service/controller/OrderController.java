package com.blubugtech.bakery_order_service.controller;

import com.blubugtech.bakery_order_service.dto.order.OrderRequest;
import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.dto.order.OrderStatusUpdateRequest;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import com.blubugtech.bakery_order_service.service.OrderService;
import com.blubugtech.common.contract.feign.HealthResponse;
import com.blubugtech.common.contract.feign.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "Order Management APIs")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Create order request received for user: {}", request.getUserId());

        if (userId != null) {
            request.setUserId(userId);
        }

        OrderResponse order = orderService.createOrder(request);

        logger.info("Order created successfully: {}", order.getOrderNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get all orders request received (page: {}, size: {})", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> orders = orderService.getAllOrders(pageable);

        logger.info("Retrieved {} orders (page {} of {})", orders.getContent().size(),
                page + 1, orders.getTotalPages());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get order by ID request received: {}", orderId);

        OrderResponse order = orderService.getOrderById(orderId);

        if (userId != null && !"ADMIN".equals(userRole) && !order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.info("Order retrieved: {}", order.getOrderNumber());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get order by number request received: {}", orderNumber);

        return orderService.getOrderByOrderNumber(orderNumber)
                .map(order -> {
                    if (userId != null && !"ADMIN".equals(userRole) && !order.getUserId().equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<OrderResponse>build();
                    }
                    logger.info("Order found: {}", orderNumber);
                    return ResponseEntity.ok(order);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @PathVariable UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get orders by user ID request received: {}", userId);

        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);

        logger.info("Retrieved {} orders for user", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<Page<OrderResponse>> getOrdersByUserIdWithPagination(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestHeader(value = "X-User-Id", required = false) UUID requestUserId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get orders by user ID with pagination: {}, page: {}, size: {}", userId, page, size);

        if (requestUserId != null && !"ADMIN".equals(userRole) && !userId.equals(requestUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> orders = orderService.getOrdersByUserIdWithPagination(userId, pageable);

        logger.info("Retrieved {} orders for user (page {} of {})", orders.getContent().size(),
                page + 1, orders.getTotalPages());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status) {

        logger.info("Get orders by status request received: {}", status);

        List<OrderResponse> orders = orderService.getOrdersByStatus(status);

        logger.info("Retrieved {} orders with status {}", orders.size(), status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(@RequestParam String query) {
        logger.info("Search orders request received with query: {}", query);

        List<OrderResponse> orders = orderService.searchOrders(query);

        logger.info("Search returned {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<OrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "7") int days) {
        logger.info("Get recent orders request received (last {} days)", days);

        List<OrderResponse> orders = orderService.getRecentOrders(days);

        logger.info("Retrieved {} recent orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<OrderResponse>> getOrdersWithFilters(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) DeliveryType deliveryType,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("Advanced filter search request received");

        List<OrderResponse> orders = orderService.getOrdersWithFilters(
                userId, status, deliveryType, paymentMethod, minAmount, maxAmount, startDate, endDate);

        logger.info("Filter search returned {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BAKER')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Update order status request received: {} to {}", orderId, request.getStatus());

        if (!"ADMIN".equals(userRole) && !"BAKER".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        OrderResponse order = orderService.updateOrderStatus(orderId, request);

        logger.info("Order status updated successfully: {}", orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Cancel order request received: {}", orderId);

        if (userId != null && !"ADMIN".equals(userRole)) {
            OrderResponse existingOrder = orderService.getOrderById(orderId);
            if (!existingOrder.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        String reason = request.get("reason");
        OrderResponse order = orderService.cancelOrder(orderId, reason);

        logger.info("Order cancelled successfully: {}", orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        logger.info("Get order statistics request received");

        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> statistics = orderService.getOrderStatistics(startDate, endDate);

        logger.info("Order statistics retrieved");
        return ResponseEntity.ok(statistics);
    }


    @PostMapping("/{orderId}/payment-update")
    public ResponseEntity<MessageResponse> updateOrderPaymentStatus(
            @PathVariable UUID orderId,
            @RequestBody Map<String, Object> paymentUpdate) {

        logger.info("Payment status update received for order: {} - Status: {}",
                orderId, paymentUpdate.get("status"));

        try {
            String paymentStatus = (String) paymentUpdate.get("status");
            OrderStatusUpdateRequest statusUpdate = new OrderStatusUpdateRequest();

            switch (paymentStatus) {
                case "COMPLETED" -> {
                    statusUpdate.setStatus(OrderStatus.CONFIRMED);
                    statusUpdate.setNotes("Payment completed successfully");
                }
                case "FAILED" -> {
                    statusUpdate.setStatus(OrderStatus.CANCELLED);
                    // reason property is not available on OrderStatusUpdateRequest, we should map notes instead?
                    // wait, order service uses "reason" or "notes"? 
                    // Let me use "notes" since OrderStatusUpdateRequest has setNotes
                    statusUpdate.setNotes("Payment failed: " + paymentUpdate.get("gatewayResponse"));
                }
                case "CANCELLED" -> {
                    statusUpdate.setStatus(OrderStatus.CANCELLED);
                    statusUpdate.setNotes("Payment cancelled");
                }
                default -> {
                    logger.info("Payment status {} for order {} - no order status change needed",
                            paymentStatus, orderId);
                }
            }

            if (statusUpdate.getStatus() != null) {
                orderService.updateOrderStatus(orderId, statusUpdate);
            }

            return ResponseEntity.ok(new MessageResponse("Payment status updated"));

        } catch (Exception e) {
            logger.error("Failed to update order payment status: {}", e.getMessage());
            return ResponseEntity.ok(new MessageResponse("Payment status acknowledged")); 
        }
    }
}
