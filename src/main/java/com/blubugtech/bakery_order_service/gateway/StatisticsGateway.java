package com.blubugtech.bakery_order_service.gateway;

import org.blubakery.common.messaging.revenue.RevenuePayload;

public interface StatisticsGateway {
    void incrementOrders();
    void decrementOrders();
    void addRevenue(RevenuePayload payload);
}
