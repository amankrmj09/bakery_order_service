package com.blubugtech.bakery_order_service.service;

import com.blubugtech.bakery_order_service.dto.order.OrderRequest;
import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.dto.order.OrderStatusUpdateRequest;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(UUID orderId);
    Optional<OrderResponse> getOrderByOrderNumber(String orderNumber);
    List<OrderResponse> getOrdersByUserId(UUID userId);
    Page<OrderResponse> getOrdersByUserIdWithPagination(UUID userId, Pageable pageable);
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
    Page<OrderResponse> getAllOrders(Pageable pageable);
    OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request);
    OrderResponse cancelOrder(UUID orderId, String reason);
    List<OrderResponse> getRecentOrders(int days);
    List<OrderResponse> searchOrders(String searchTerm);
    List<OrderResponse> getOrdersWithFilters(UUID userId, OrderStatus status, DeliveryType deliveryType, String paymentMethod, BigDecimal minAmount, BigDecimal maxAmount, LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Object> getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
