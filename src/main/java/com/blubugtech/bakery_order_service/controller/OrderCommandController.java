package com.blubugtech.bakery_order_service.controller;

import lombok.extern.slf4j.Slf4j;
import com.blubugtech.bakery_order_service.dto.order.OrderRequest;
import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.dto.order.OrderStatusUpdateRequest;
import com.blubugtech.bakery_order_service.service.OrderCommandService;
import org.blubakery.common.feign.contract.feign.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Commands", description = "Order Command APIs")
@Slf4j
public class OrderCommandController {

    private final OrderCommandService orderCommandService;

    public OrderCommandController(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {

        log.info("Create order request received for user: {}", request.getUserId());

        if (userId != null) {
            request.setUserId(userId);
        }

        OrderResponse order = orderCommandService.createOrder(request);

        log.info("Order created successfully: {}", order.getOrderNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'BAKER')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        log.info("Update order status request received: {} to {}", orderId, request.getStatus());

        OrderResponse order = orderCommandService.updateOrderStatus(orderId, request);

        log.info("Order status updated successfully: {}", orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestParam(value = "reason", required = false) String paramReason,
            @RequestBody(required = false) Map<String, String> request) {

        log.info("Cancel order request received: {}", orderId);

        String reason = "User requested cancellation";
        if (paramReason != null && !paramReason.trim().isEmpty()) {
            reason = paramReason;
        } else if (request != null && request.get("reason") != null && !request.get("reason").trim().isEmpty()) {
            reason = request.get("reason");
        }
        
        OrderResponse order = orderCommandService.cancelOrder(orderId, reason, false);

        log.info("Order cancelled successfully: {}", orderId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/payment-update")
    public ResponseEntity<MessageResponse> updateOrderPaymentStatus(
            @PathVariable UUID orderId,
            @RequestBody Map<String, Object> paymentUpdate) {

        log.info("Payment status update received for order: {} - Status: {}",
                orderId, paymentUpdate.get("status"));

        try {
            String paymentStatus = (String) paymentUpdate.get("status");
            String notes = paymentUpdate.get("gatewayResponse") != null ? (String) paymentUpdate.get("gatewayResponse") : "Payment status update";
            orderCommandService.updatePaymentStatus(orderId, paymentStatus, notes);

            return ResponseEntity.ok(new MessageResponse("Payment status updated"));

        } catch (Exception e) {
            log.error("Failed to update order payment status", e);
            return ResponseEntity.ok(new MessageResponse("Payment status acknowledged")); 
        }
    }
}
