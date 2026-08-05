package com.blubugtech.bakery_order_service.service;

import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderQueryService {
    OrderResponse getOrderById(UUID orderId);
    Optional<OrderResponse> getOrderByOrderNumber(String orderNumber);
    PagedModel<OrderResponse> getOrdersByUserId(UUID userId, Pageable pageable);
    PagedModel<OrderResponse> getActiveOrdersByUserId(UUID userId, Pageable pageable);
    PagedModel<OrderResponse> getOrdersByUserIdWithPagination(UUID userId, Pageable pageable);
    PagedModel<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);
    PagedModel<OrderResponse> getAllOrders(Pageable pageable);
    PagedModel<OrderResponse> getRecentOrders(int days, Pageable pageable);
    PagedModel<OrderResponse> searchOrders(String searchTerm, Pageable pageable);
    PagedModel<OrderResponse> getOrdersWithFilters(UUID userId, OrderStatus status, DeliveryType deliveryType, String paymentMethod, BigDecimal minAmount, BigDecimal maxAmount, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
