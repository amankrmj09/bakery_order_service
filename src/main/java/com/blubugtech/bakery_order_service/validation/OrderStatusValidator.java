package com.blubugtech.bakery_order_service.validation;

import com.blubugtech.bakery_order_service.enums.OrderStatus;
import com.blubugtech.bakery_order_service.exception.InvalidOrderStatusException;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusValidator {

    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValidTransition = false;
        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
            isValidTransition = false; // Terminal states
        } else if (newStatus == OrderStatus.CANCELLED) {
            isValidTransition = true; // Can cancel anytime before delivery
        } else {
            // Allow skipping forward (e.g. CONFIRMED directly to DELIVERED)
            isValidTransition = newStatus.ordinal() > currentStatus.ordinal();
        }

        if (!isValidTransition) {
            throw new InvalidOrderStatusException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }
}
