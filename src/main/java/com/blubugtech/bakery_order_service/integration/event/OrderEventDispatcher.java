package com.blubugtech.bakery_order_service.integration.event;

import com.blubugtech.common.event.OrderEvent;
import com.blubugtech.bakery_order_service.integration.kafka.producer.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventDispatcher {

    private final OrderEventPublisher orderEventPublisher;

    public void dispatchOrderCreated(OrderEvent event) {
        // Dispatch domain events. Right now it just sends to Kafka,
        // but this could also involve webhooks or audit logging in the future.
        orderEventPublisher.publishOrderCreated(event);
    }

    public void dispatchOrderStatusUpdated(OrderEvent event) {
        orderEventPublisher.publishOrderStatusUpdated(event);
    }
}
