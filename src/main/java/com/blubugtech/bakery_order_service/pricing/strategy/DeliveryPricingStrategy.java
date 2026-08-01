package com.blubugtech.bakery_order_service.pricing.strategy;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.DeliveryType;

import java.math.BigDecimal;

public interface DeliveryPricingStrategy {
    boolean supports(DeliveryType deliveryType);
    BigDecimal calculateFee(Order order);
}
