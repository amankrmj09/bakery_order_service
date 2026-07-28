package com.blubugtech.bakery_order_service.gateway;

import org.blubakery.bakery_common_libs.contract.messaging.RevenuePayload;

public interface StatisticsGateway {
    void incrementOrders();
    void decrementOrders();
    void addRevenue(RevenuePayload payload);
}
