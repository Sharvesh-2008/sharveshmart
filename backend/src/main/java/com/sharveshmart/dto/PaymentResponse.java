package com.sharveshmart.dto;

import com.sharveshmart.entity.Payment;
import com.sharveshmart.entity.PaymentStatus;

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
