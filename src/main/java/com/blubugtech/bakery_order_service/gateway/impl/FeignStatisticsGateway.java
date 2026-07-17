package com.blubugtech.bakery_order_service.gateway.impl;

import com.blubugtech.bakery_order_service.client.statistics.InternalStatsClient;
import com.blubugtech.bakery_order_service.gateway.StatisticsGateway;
import com.blubugtech.common.contract.messaging.RevenuePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeignStatisticsGateway implements StatisticsGateway {

    private final InternalStatsClient internalStatsClient;

    @Override
    public void incrementOrders() {
        internalStatsClient.incrementOrders();
    }

    @Override
    public void decrementOrders() {
        internalStatsClient.decrementOrders();
    }

    @Override
    public void addRevenue(RevenuePayload payload) {
        internalStatsClient.addRevenue(payload);
    }
}
