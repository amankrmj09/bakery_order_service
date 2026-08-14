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
import org.blubakery.common.core.dto.RestPageResponse;
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
    public ResponseEntity<RestPageResponse<OrderResponse>> getOrdersByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get orders by user ID request received: {}", userId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        RestPageResponse<OrderResponse> orders = orderQueryService.getOrdersByUserId(userId, pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<RestPageResponse<OrderResponse>> getActiveOrdersByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get active orders by user ID request received: {}", userId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        RestPageResponse<OrderResponse> orders = orderQueryService.getActiveOrdersByUserId(userId, pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<RestPageResponse<OrderResponse>> getOrdersByUserIdWithPagination(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get orders by user ID with pagination: {}, page: {}, size: {}", userId, page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        RestPageResponse<OrderResponse> orders = orderQueryService.getOrdersByUserIdWithPagination(userId, pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<RestPageResponse<OrderResponse>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get orders by status request received: {}", status);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        RestPageResponse<OrderResponse> orders = orderQueryService.getOrdersByStatus(status, pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/search")
    public ResponseEntity<RestPageResponse<OrderResponse>> searchOrders(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
            
        log.info("Search orders request received with query: {}", query);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        RestPageResponse<OrderResponse> orders = orderQueryService.searchOrders(query, pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/recent")
    public ResponseEntity<RestPageResponse<OrderResponse>> getRecentOrders(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
            
        log.info("Get recent orders request received (last {} days)", days);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        RestPageResponse<OrderResponse> orders = orderQueryService.getRecentOrders(days, pageable);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/filter")
    public ResponseEntity<RestPageResponse<OrderResponse>> getOrdersWithFilters(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) DeliveryType deliveryType,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Advanced filter search request received");

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        RestPageResponse<OrderResponse> orders = orderQueryService.getOrdersWithFilters(
                userId, status, deliveryType, paymentMethod, minAmount, maxAmount, startDate, endDate, pageable);

        return ResponseEntity.ok(orders);
    }
}
