package com.digitalmarketplace.dto;

import com.digitalmarketplace.entity.PurchaseEntitlement;

import java.time.LocalDateTime;

public record LibraryItemResponse(
        Long id,
        Long productId,
        String productTitle,
        Long orderId,
        LocalDateTime grantedAt
) {

    public static LibraryItemResponse from(PurchaseEntitlement entitlement) {
        return new LibraryItemResponse(
                entitlement.getId(),
                entitlement.getProduct().getId(),
                entitlement.getProduct().getTitle(),
                entitlement.getOrder().getId(),
                entitlement.getGrantedAt()
        );
    }
}
