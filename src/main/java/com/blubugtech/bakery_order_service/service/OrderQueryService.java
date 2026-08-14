package com.blubugtech.bakery_order_service.service;

import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.blubakery.common.core.dto.RestPageResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderQueryService {
    OrderResponse getOrderById(UUID orderId);
    Optional<OrderResponse> getOrderByOrderNumber(String orderNumber);
    RestPageResponse<OrderResponse> getOrdersByUserId(UUID userId, Pageable pageable);
    RestPageResponse<OrderResponse> getActiveOrdersByUserId(UUID userId, Pageable pageable);
    RestPageResponse<OrderResponse> getOrdersByUserIdWithPagination(UUID userId, Pageable pageable);
    RestPageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);
    RestPageResponse<OrderResponse> getAllOrders(Pageable pageable);
    RestPageResponse<OrderResponse> getRecentOrders(int days, Pageable pageable);
    RestPageResponse<OrderResponse> searchOrders(String searchTerm, Pageable pageable);
    RestPageResponse<OrderResponse> getOrdersWithFilters(UUID userId, OrderStatus status, DeliveryType deliveryType, String paymentMethod, BigDecimal minAmount, BigDecimal maxAmount, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
