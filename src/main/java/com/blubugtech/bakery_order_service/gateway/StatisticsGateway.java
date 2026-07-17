package com.blubugtech.bakery_order_service.gateway;

import com.blubugtech.common.contract.messaging.RevenuePayload;

public interface StatisticsGateway {
    void incrementOrders();
    void decrementOrders();
    void addRevenue(RevenuePayload payload);
}
