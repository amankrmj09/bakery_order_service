package com.blubugtech.bakery_order_service.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import org.blubakery.common.feign.contract.feign.Product;
import org.blubakery.common.feign.contract.feign.StockAvailability;
import org.blubakery.common.messaging.stock.StockOperationRequestPayload;
import org.blubakery.common.messaging.stock.StockOperationResponsePayload;
import java.util.UUID;

@FeignClient(name = "bakery-product-service", path = "/api", fallbackFactory = ProductServiceClientFallbackFactory.class)
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    Product getProductById(@PathVariable UUID productId);

    @GetMapping("/inventory/product/{productId}/availability")
    StockAvailability checkStockAvailability(@PathVariable UUID productId, @RequestParam Integer quantity);

    @PostMapping("/inventory/product/{productId}/reserve")
    StockOperationResponsePayload reserveStock(@PathVariable UUID productId, @RequestBody StockOperationRequestPayload request);

    @PostMapping("/inventory/product/{productId}/release-reserved")
    StockOperationResponsePayload releaseReservedStock(@PathVariable UUID productId, @RequestBody StockOperationRequestPayload request);

    @PostMapping("/inventory/product/{productId}/consume")
    StockOperationResponsePayload consumeStock(@PathVariable UUID productId, @RequestBody StockOperationRequestPayload request);
}
