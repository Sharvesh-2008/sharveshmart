package com.digitalmarketplace.service.payment;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentResult process(PaymentRequest request) {
        return PaymentResult.success("MOCK-" + UUID.randomUUID());
    }
}