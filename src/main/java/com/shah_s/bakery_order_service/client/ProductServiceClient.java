package com.shah_s.bakery_order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "bakery-product-service", path = "/api")
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    Map<String, Object> getProductById(@PathVariable UUID productId);

    @GetMapping("/inventory/product/{productId}/availability")
    Map<String, Object> checkStockAvailability(@PathVariable UUID productId, @RequestParam Integer quantity);

    @PostMapping("/inventory/product/{productId}/reserve")
    Map<String, Object> reserveStock(@PathVariable UUID productId, @RequestBody Map<String, Integer> request);

    @PostMapping("/inventory/product/{productId}/release-reserved")
    Map<String, Object> releaseReservedStock(@PathVariable UUID productId, @RequestBody Map<String, Integer> request);

    @PostMapping("/inventory/product/{productId}/consume")
    Map<String, Object> consumeStock(@PathVariable UUID productId, @RequestBody Map<String, Integer> request);
}
