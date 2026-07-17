package com.blubugtech.bakery_order_service.client.product;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.blubugtech.common.contract.feign.Product;
import com.blubugtech.common.contract.feign.StockAvailability;
import com.blubugtech.common.contract.messaging.StockOperationRequestPayload;
import com.blubugtech.common.contract.messaging.StockOperationResponsePayload;
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
