package com.sharveshmart.dto;

import com.sharveshmart.entity.Order;
import com.sharveshmart.entity.OrderItem;
import com.sharveshmart.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {

    public static OrderDetailResponse from(Order order, List<OrderItem> items) {
        return new OrderDetailResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                items.stream().map(OrderItemResponse::from).toList()
        );
    }
}
