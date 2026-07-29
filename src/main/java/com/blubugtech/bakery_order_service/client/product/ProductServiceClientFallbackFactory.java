package com.blubugtech.bakery_order_service.client.product;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.bakery_common_libs.contract.feign.Product;
import org.blubakery.bakery_common_libs.contract.feign.StockAvailability;
import org.blubakery.bakery_common_libs.contract.messaging.StockOperationRequestPayload;
import org.blubakery.bakery_common_libs.contract.messaging.StockOperationResponsePayload;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ProductServiceClientFallbackFactory implements FallbackFactory<ProductServiceClient> {

    @Override
    public ProductServiceClient create(Throwable cause) {
        return new ProductServiceClient() {
            @Override
            public Product getProductById(UUID productId) {
                
                log.error("Fallback triggered for getProductById: {}", productId, cause);
                return null;
            }

            @Override
            public StockAvailability checkStockAvailability(UUID productId, Integer quantity) {
                
                log.error("Fallback triggered for checkStockAvailability: {} for qty {}", productId, quantity, cause);
                StockAvailability dto = new StockAvailability();
                dto.setSufficient(false);
                dto.setAvailableQuantity(0);
                return dto;
            }

            @Override
            public StockOperationResponsePayload reserveStock(UUID productId, StockOperationRequestPayload request) {
                
                log.error("Fallback triggered for reserveStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public StockOperationResponsePayload releaseReservedStock(UUID productId, StockOperationRequestPayload request) {
                
                log.error("Fallback triggered for releaseReservedStock: {}", productId, cause);
                return createErrorResponse(productId);
            }

            @Override
            public StockOperationResponsePayload consumeStock(UUID productId, StockOperationRequestPayload request) {
                
                log.error("Fallback triggered for consumeStock: {}", productId, cause);
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
