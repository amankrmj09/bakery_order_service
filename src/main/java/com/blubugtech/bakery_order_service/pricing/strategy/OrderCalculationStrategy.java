package com.blubugtech.bakery_order_service.pricing.strategy;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.DeliveryType;

public interface OrderCalculationStrategy {
    boolean supports(DeliveryType deliveryType);
    void calculateTotals(Order order);
}
