package com.blubugtech.bakery_order_service.gateway.impl;

import com.blubugtech.bakery_order_service.client.product.ProductServiceClient;
import com.blubugtech.bakery_order_service.gateway.ProductGateway;
import org.blubakery.common.feign.contract.feign.Product;
import org.blubakery.common.feign.contract.feign.StockAvailability;
import org.blubakery.common.messaging.stock.StockOperationRequestPayload;
import org.blubakery.common.messaging.stock.StockOperationResponsePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FeignProductGateway implements ProductGateway {

    private final ProductServiceClient productServiceClient;

    @Override
    public Product getProductById(UUID productId) {
        return productServiceClient.getProductById(productId);
    }

    @Override
    public StockAvailability checkStockAvailability(UUID productId, Integer quantity) {
        return productServiceClient.checkStockAvailability(productId, quantity);
    }

    @Override
    public StockOperationResponsePayload reserveStock(UUID productId, StockOperationRequestPayload request) {
        return productServiceClient.reserveStock(productId, request);
    }

    @Override
    public StockOperationResponsePayload releaseReservedStock(UUID productId, StockOperationRequestPayload request) {
        return productServiceClient.releaseReservedStock(productId, request);
    }

    @Override
    public StockOperationResponsePayload consumeStock(UUID productId, StockOperationRequestPayload request) {
        return productServiceClient.consumeStock(productId, request);
    }
}
