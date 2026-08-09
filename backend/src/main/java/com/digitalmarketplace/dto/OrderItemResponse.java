package com.digitalmarketplace.dto;

import com.digitalmarketplace.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productTitle,
        BigDecimal unitPrice,
        int quantity
) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getTitle(),
                item.getUnitPrice(),
                item.getQuantity()
        );
    }
}
