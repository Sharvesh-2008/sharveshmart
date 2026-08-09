package com.digitalmarketplace.service.payment;

public interface PaymentProcessor {

    PaymentResult process(PaymentRequest request);
}