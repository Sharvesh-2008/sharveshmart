package com.sharveshmart.service.payment;

public interface PaymentProcessor {

    PaymentResult process(PaymentRequest request);
}