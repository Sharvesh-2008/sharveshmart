package com.digitalmarketplace.dto;

import com.digitalmarketplace.entity.CartItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItemResponse(
        Long id,
        Long productId,
        String productTitle,
        BigDecimal unitPrice,
        int quantity,
        LocalDateTime addedAt
) {

    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getTitle(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getAddedAt()
        );
    }
}
