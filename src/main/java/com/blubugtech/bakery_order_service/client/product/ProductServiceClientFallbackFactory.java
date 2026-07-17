package com.blubugtech.bakery_order_service.client.product;

import com.blubugtech.common.contract.feign.Product;
import com.blubugtech.common.contract.feign.StockAvailability;
import com.blubugtech.common.contract.messaging.StockOperationRequestPayload;
import com.blubugtech.common.contract.messaging.StockOperationResponsePayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductServiceClientFallbackFactory implements FallbackFactory<ProductServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceClientFallbackFactory.class);

    @Override
    public ProductServiceClient create(Throwable cause) {
        return new ProductServiceClient() {
            @Override
            public Product getProductById(UUID productId) {
                
                logger.error("Fallback triggered for getProductById: {}", productId, cause);
                return null;
            }

            @Override
            public StockAvailability checkStockAvailability(UUID productId, Integer quantity) {
                
                logger.error("Fallback triggered for checkStockAvailability: {} for qty {}", productId, quantity, cause);
                StockAvailability dto = new StockAvailability();
                dto.setSufficient(false);
                dto.setAvailableQuantity(0);
                return dto;
            }

            @Override
            public StockOperationResponsePayload reserveStock(UUID productId, StockOperationRequestPayload request) {
                
                logger.error("Fallback triggered for reserveStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public StockOperationResponsePayload releaseReservedStock(UUID productId, StockOperationRequestPayload request) {
                
                logger.error("Fallback triggered for releaseReservedStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public StockOperationResponsePayload consumeStock(UUID productId, StockOperationRequestPayload request) {
                
                logger.error("Fallback triggered for consumeStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            private StockOperationResponsePayload createErrorResponse(UUID productId) {
                StockOperationResponsePayload dto = new StockOperationResponsePayload();
                dto.setProductId(productId);
                dto.setSuccess(false);
                dto.setMessage("Service unavailable");
                return dto;
            }
        };
    }
}
