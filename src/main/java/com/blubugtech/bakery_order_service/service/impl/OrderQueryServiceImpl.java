package com.blubugtech.bakery_order_service.service.impl;

import com.blubugtech.bakery_order_service.dto.order.OrderResponse;
import com.blubugtech.bakery_order_service.entity.Order;
import com.blubugtech.bakery_order_service.enums.DeliveryType;
import com.blubugtech.bakery_order_service.enums.OrderStatus;
import com.blubugtech.bakery_order_service.exception.OrderServiceException;
import com.blubugtech.bakery_order_service.mapper.OrderMapper;
import com.blubugtech.bakery_order_service.repository.OrderRepository;
import com.blubugtech.bakery_order_service.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderServiceException("Order not found with ID: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    public Optional<OrderResponse> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).map(orderMapper::toResponse);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getActiveOrdersByUserId(UUID userId) {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
                OrderStatus.READY, OrderStatus.OUT_FOR_DELIVERY
        );
        return orderRepository.findByUserIdAndStatusIn(userId, activeStatuses).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponse> getOrdersByUserIdWithPagination(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper::toResponse);
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    @Override
    public List<OrderResponse> getRecentOrders(int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return orderRepository.findRecentOrders(sinceDate).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> searchOrders(String searchTerm) {
        return orderRepository.searchByCustomerInfo(searchTerm).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersWithFilters(UUID userId, OrderStatus status, DeliveryType deliveryType, String paymentMethod, BigDecimal minAmount, BigDecimal maxAmount, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersWithFilters(userId, status, deliveryType, null, minAmount, maxAmount, startDate, endDate).stream()
                .map(orderMapper::toResponse).collect(Collectors.toList());
    }
}
