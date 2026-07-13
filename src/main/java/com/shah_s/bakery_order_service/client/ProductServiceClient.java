package com.shah_s.bakery_order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import org.devofblue.common.dto.ProductDto;
import org.devofblue.common.dto.StockAvailabilityDto;
import org.devofblue.common.dto.StockOperationRequestDto;
import org.devofblue.common.dto.StockOperationResponseDto;
import java.util.UUID;

@FeignClient(name = "bakery-product-service", path = "/api")
public interface ProductServiceClient {

    @GetMapping("/products/{productId}")
    ProductDto getProductById(@PathVariable UUID productId);

    @GetMapping("/inventory/product/{productId}/availability")
    StockAvailabilityDto checkStockAvailability(@PathVariable UUID productId, @RequestParam Integer quantity);

    @PostMapping("/inventory/product/{productId}/reserve")
    StockOperationResponseDto reserveStock(@PathVariable UUID productId, @RequestBody StockOperationRequestDto request);

    @PostMapping("/inventory/product/{productId}/release-reserved")
    StockOperationResponseDto releaseReservedStock(@PathVariable UUID productId, @RequestBody StockOperationRequestDto request);

    @PostMapping("/inventory/product/{productId}/consume")
    StockOperationResponseDto consumeStock(@PathVariable UUID productId, @RequestBody StockOperationRequestDto request);
}
