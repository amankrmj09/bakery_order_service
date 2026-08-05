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
import org.springframework.data.web.PagedModel;
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
    public PagedModel<OrderResponse> getOrdersByUserId(UUID userId, Pageable pageable) {
        Page<OrderResponse> page = orderRepository.findByUserId(userId, pageable).map(orderMapper::toResponse);
        return new PagedModel<>(page);
    }

    @Override
    public PagedModel<OrderResponse> getActiveOrdersByUserId(UUID userId, Pageable pageable) {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
                OrderStatus.READY, OrderStatus.OUT_FOR_DELIVERY
        );
        Page<OrderResponse> page = orderRepository.findByUserIdAndStatusIn(userId, activeStatuses, pageable)
                .map(orderMapper::toResponse);
        return new PagedModel<>(page);
    }

    @Override
    public PagedModel<OrderResponse> getOrdersByUserIdWithPagination(UUID userId, Pageable pageable) {
        Page<OrderResponse> page = orderRepository.findByUserId(userId, pageable).map(orderMapper::toResponse);
        return new PagedModel<>(page);
    }

    @Override
    public PagedModel<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<OrderResponse> page = orderRepository.findByStatus(status, pageable)
                .map(orderMapper::toResponse);
        return new PagedModel<>(page);
    }

    @Override
    public PagedModel<OrderResponse> getAllOrders(Pageable pageable) {
        Page<OrderResponse> page = orderRepository.findAll(pageable).map(orderMapper::toResponse);
        return new PagedModel<>(page);
    }

    @Override
    public PagedModel<OrderResponse> getRecentOrders(int days, Pageable pageable) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        Page<OrderResponse> page = orderRepository.findRecentOrders(sinceDate, pageable)
                .map(orderMapper::toResponse);
        return new PagedModel<>(page);
    }

    @Override
    public PagedModel<OrderResponse> searchOrders(String searchTerm, Pageable pageable) {
        Page<OrderResponse> page = orderRepository.searchByCustomerInfo(searchTerm, pageable)
                .map(orderMapper::toResponse);
        return new PagedModel<>(page);
    }

    @Override
    public PagedModel<OrderResponse> getOrdersWithFilters(UUID userId, OrderStatus status, DeliveryType deliveryType, String paymentMethod, BigDecimal minAmount, BigDecimal maxAmount, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<OrderResponse> page = orderRepository.findOrdersWithFilters(userId, status, deliveryType, null, minAmount, maxAmount, startDate, endDate, pageable)
                .map(orderMapper::toResponse);
        return new PagedModel<>(page);
    }
}
