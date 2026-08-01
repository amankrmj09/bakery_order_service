package com.blubugtech.bakery_order_service.service.impl;

import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import com.blubugtech.bakery_order_service.repository.OrderRepository;
import com.blubugtech.bakery_order_service.service.OrderAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderAnalyticsServiceImpl implements OrderAnalyticsService {

    private final OrderRepository orderRepository;

    @Override
    public Map<String, Object> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        long totalOrders = orderRepository.countByCreatedAtBetween(startDate, endDate);
        long pendingOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.PENDING, startDate, endDate);
        long completedOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.DELIVERED, startDate, endDate);
        long cancelledOrders = orderRepository.countByStatusAndCreatedAtBetween(OrderStatus.CANCELLED, startDate, endDate);

        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = totalOrders > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        return Map.of(
                "totalOrders", totalOrders,
                "totalRevenue", totalRevenue,
                "averageOrderValue", averageOrderValue,
                "pendingOrders", pendingOrders,
                "completedOrders", completedOrders,
                "cancelledOrders", cancelledOrders,
                "dateRange", Map.of("startDate", startDate.toString(), "endDate", endDate.toString())
        );
    }
}
