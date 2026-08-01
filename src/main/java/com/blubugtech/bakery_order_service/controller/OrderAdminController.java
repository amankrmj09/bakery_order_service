package com.blubugtech.bakery_order_service.controller;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.service.OrderCommandService;
import com.blubugtech.bakery_order_service.service.OrderQueryService;
import com.blubugtech.bakery_order_service.service.OrderAnalyticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Admin", description = "Order Admin APIs")
@Slf4j
public class OrderAdminController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final OrderAnalyticsService orderAnalyticsService;

    public OrderAdminController(OrderCommandService orderCommandService,
                                OrderQueryService orderQueryService,
                                OrderAnalyticsService orderAnalyticsService) {
        this.orderCommandService = orderCommandService;
        this.orderQueryService = orderQueryService;
        this.orderAnalyticsService = orderAnalyticsService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Get all orders request received (page: {}, size: {})", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderResponse> orders = orderQueryService.getAllOrders(pageable);

        log.info("Retrieved {} orders (page {} of {})", orders.getContent().size(),
                page + 1, orders.getTotalPages());
        return ResponseEntity.ok(new PagedModel<>(orders));
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> searchOrdersAdmin(@RequestParam String query) {
        log.info("Admin search orders request received with query: {}", query);
        List<OrderResponse> orders = orderQueryService.searchOrders(query);
        log.info("Admin search returned {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/admin-cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> adminCancelOrder(
            @PathVariable UUID orderId,
            @RequestParam(value = "reason", required = false) String paramReason,
            @RequestBody(required = false) Map<String, String> request) {

        log.info("Admin cancel order request received: {}", orderId);

        String reason = "Admin requested cancellation";
        if (paramReason != null && !paramReason.trim().isEmpty()) {
            reason = paramReason;
        } else if (request != null && request.get("reason") != null && !request.get("reason").trim().isEmpty()) {
            reason = request.get("reason");
        }
        OrderResponse order = orderCommandService.cancelOrder(orderId, reason, true);

        log.info("Order cancelled successfully: {}", orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Get order statistics request received");

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> statistics = orderAnalyticsService.getOrderStatistics(startDate, endDate);

        log.info("Order statistics retrieved");
        return ResponseEntity.ok(statistics);
    }
}
