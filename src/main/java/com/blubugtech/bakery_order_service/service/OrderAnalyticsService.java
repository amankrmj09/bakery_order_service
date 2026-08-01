package com.blubugtech.bakery_order_service.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface OrderAnalyticsService {
    Map<String, Object> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
