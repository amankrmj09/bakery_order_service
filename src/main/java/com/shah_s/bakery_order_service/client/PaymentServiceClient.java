package com.shah_s.bakery_order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "bakery-payment-service", path = "/api/payments")
public interface PaymentServiceClient {

    @PostMapping
    Map<String, Object> createPayment(@RequestBody Map<String, Object> request);

    @GetMapping("/{paymentId}")
    Map<String, Object> getPaymentById(@PathVariable UUID paymentId);

    @GetMapping("/order/{orderId}")
    Map<String, Object> getPaymentByOrderId(@PathVariable UUID orderId);

    @PatchMapping("/{paymentId}/status")
    Map<String, Object> updatePaymentStatus(@PathVariable UUID paymentId, @RequestBody Map<String, String> request);

    @PostMapping("/{paymentId}/cancel")
    Map<String, Object> cancelPayment(@PathVariable UUID paymentId, @RequestBody Map<String, String> request);
}
