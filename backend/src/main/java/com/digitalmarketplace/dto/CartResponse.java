package com.digitalmarketplace.dto;

import com.digitalmarketplace.entity.CartItem;

import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        List<CartItemResponse> items
) {

    public static CartResponse from(Long cartId, Long userId, List<CartItem> items) {
        return new CartResponse(
                cartId,
                userId,
                items.stream().map(CartItemResponse::from).toList()
        );
    }
}
