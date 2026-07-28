package com.blubugtech.bakery_order_service.gateway;

import org.blubakery.bakery_common_libs.contract.feign.Product;
import org.blubakery.bakery_common_libs.contract.feign.StockAvailability;
import org.blubakery.bakery_common_libs.contract.messaging.StockOperationRequestPayload;
import org.blubakery.bakery_common_libs.contract.messaging.StockOperationResponsePayload;

import java.util.UUID;

public interface ProductGateway {
    Product getProductById(UUID productId);
    StockAvailability checkStockAvailability(UUID productId, Integer quantity);
    StockOperationResponsePayload reserveStock(UUID productId, StockOperationRequestPayload request);
    StockOperationResponsePayload releaseReservedStock(UUID productId, StockOperationRequestPayload request);
    StockOperationResponsePayload consumeStock(UUID productId, StockOperationRequestPayload request);
}
