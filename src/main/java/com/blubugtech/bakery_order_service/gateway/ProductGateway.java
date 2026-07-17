package com.blubugtech.bakery_order_service.gateway;

import com.blubugtech.common.contract.feign.Product;
import com.blubugtech.common.contract.feign.StockAvailability;
import com.blubugtech.common.contract.messaging.StockOperationRequestPayload;
import com.blubugtech.common.contract.messaging.StockOperationResponsePayload;

import java.util.UUID;

public interface ProductGateway {
    Product getProductById(UUID productId);
    StockAvailability checkStockAvailability(UUID productId, Integer quantity);
    StockOperationResponsePayload reserveStock(UUID productId, StockOperationRequestPayload request);
    StockOperationResponsePayload releaseReservedStock(UUID productId, StockOperationRequestPayload request);
    StockOperationResponsePayload consumeStock(UUID productId, StockOperationRequestPayload request);
}
