package com.blubugtech.bakery_order_service.service;

import java.time.LocalDateTime;
import java.util.Map;

public interface OrderAnalyticsService {
    com.blubugtech.bakery_order_service.dto.OrderStatisticsResponse getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
