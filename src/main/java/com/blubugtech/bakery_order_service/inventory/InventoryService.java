package com.blubugtech.bakery_order_service.inventory;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.entity.OrderItem;
import com.blubugtech.bakery_order_service.exception.OrderServiceException;
import com.blubugtech.bakery_order_service.gateway.ProductGateway;
import com.blubugtech.common.contract.messaging.StockOperationRequestPayload;
import com.blubugtech.common.contract.messaging.StockOperationResponsePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductGateway productGateway;

    public void reserveStockForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            try {
                StockOperationRequestPayload request = new StockOperationRequestPayload(item.getQuantity());
                StockOperationResponsePayload response = productGateway.reserveStock(item.getProductId(), request);
                Boolean success = response.getSuccess();

                if (success == null || !success) {
                    throw new OrderServiceException("Failed to reserve stock for product: " + item.getProductName());
                }
            } catch (Exception e) {
                throw new OrderServiceException("Stock reservation failed: " + e.getMessage());
            }
        }
    }

    public void releaseStockForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            try {
                StockOperationRequestPayload request = new StockOperationRequestPayload(item.getQuantity());
                productGateway.releaseReservedStock(item.getProductId(), request);
            } catch (Exception e) {
                log.error("Failed to release stock for product {}: {}", item.getProductId(), e.getMessage());
            }
        }
    }

    public void consumeStockForOrder(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            try {
                StockOperationRequestPayload request = new StockOperationRequestPayload(item.getQuantity());
                productGateway.consumeStock(item.getProductId(), request);
            } catch (Exception e) {
                log.error("Failed to consume stock for product {}: {}", item.getProductId(), e.getMessage());
                throw new OrderServiceException("Stock consumption failed: " + e.getMessage());
            }
        }
    }
}
