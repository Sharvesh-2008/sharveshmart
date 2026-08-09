package com.digitalmarketplace.dto;

import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt
) {

    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
