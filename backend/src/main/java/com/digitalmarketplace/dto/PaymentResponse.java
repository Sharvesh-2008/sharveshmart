package com.digitalmarketplace.dto;

import com.digitalmarketplace.entity.Payment;
import com.digitalmarketplace.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status,
        String method,
        String providerReference,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getProviderReference(),
                payment.getCreatedAt(),
                payment.getPaidAt()
        );
    }
}
