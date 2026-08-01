package com.blubugtech.bakery_order_service.controller;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import com.blubugtech.bakery_order_service.service.OrderQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Queries", description = "Order Query APIs")
@Slf4j
public class OrderQueryController {

    private final OrderQueryService orderQueryService;

    public OrderQueryController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {

        log.info("Get order by ID request received: {}", orderId);

        OrderResponse order = orderQueryService.getOrderById(orderId);

        log.info("Order retrieved: {}", order.getOrderNumber());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(
            @PathVariable String orderNumber) {

        log.info("Get order by number request received: {}", orderNumber);

        return orderQueryService.getOrderByOrderNumber(orderNumber)
                .map(order -> {
                    log.info("Order found: {}", orderNumber);
                    return ResponseEntity.ok(order);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @PathVariable UUID userId) {

        log.info("Get orders by user ID request received: {}", userId);

        List<OrderResponse> orders = orderQueryService.getOrdersByUserId(userId);

        log.info("Retrieved {} orders for user", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<OrderResponse>> getActiveOrdersByUserId(
            @PathVariable UUID userId) {

        log.info("Get active orders by user ID request received: {}", userId);

        List<OrderResponse> orders = orderQueryService.getActiveOrdersByUserId(userId);

        log.info("Retrieved {} active orders for user", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<PagedModel<OrderResponse>> getOrdersByUserIdWithPagination(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get orders by user ID with pagination: {}, page: {}, size: {}", userId, page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> orders = orderQueryService.getOrdersByUserIdWithPagination(userId, pageable);

        log.info("Retrieved {} orders for user (page {} of {})", orders.getContent().size(),
                page + 1, orders.getTotalPages());
        return ResponseEntity.ok(new PagedModel<>(orders));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status) {

        log.info("Get orders by status request received: {}", status);

        List<OrderResponse> orders = orderQueryService.getOrdersByStatus(status);

        log.info("Retrieved {} orders with status {}", orders.size(), status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(@RequestParam String query) {
        log.info("Search orders request received with query: {}", query);

        List<OrderResponse> orders = orderQueryService.searchOrders(query);

        log.info("Search returned {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<OrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Get recent orders request received (last {} days)", days);

        List<OrderResponse> orders = orderQueryService.getRecentOrders(days);

        log.info("Retrieved {} recent orders", orders.size());
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

        log.info("Advanced filter search request received");

        List<OrderResponse> orders = orderQueryService.getOrdersWithFilters(
                userId, status, deliveryType, paymentMethod, minAmount, maxAmount, startDate, endDate);

        log.info("Filter search returned {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }
}
