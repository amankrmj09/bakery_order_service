package com.blubugtech.bakery_order_service.client.product;

import lombok.extern.slf4j.Slf4j;
import org.blubakery.common.feign.contract.feign.Product;
import org.blubakery.common.feign.contract.feign.StockAvailability;
import org.blubakery.common.messaging.stock.StockOperationRequestPayload;
import org.blubakery.common.messaging.stock.StockOperationResponsePayload;

import org.blubakery.common.feign.exception.common.FeignClientException;
import org.blubakery.common.core.exception.common.ServiceUnavailableException;
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
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for getProductById: {}", productId, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public StockAvailability checkStockAvailability(UUID productId, Integer quantity) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for checkStockAvailability: {} for qty {}", productId, quantity, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public StockOperationResponsePayload reserveStock(UUID productId, StockOperationRequestPayload request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for reserveStock: {}", productId, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public StockOperationResponsePayload releaseReservedStock(UUID productId, StockOperationRequestPayload request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for releaseReservedStock: {}", productId, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }

            @Override
            public StockOperationResponsePayload consumeStock(UUID productId, StockOperationRequestPayload request) {
                if (cause instanceof FeignClientException) throw (FeignClientException) cause;
                log.error("Fallback triggered for consumeStock: {}", productId, cause);
                throw new ServiceUnavailableException("Product Service is currently unavailable. Please try again later.", cause);
            }
        };
    }
}
