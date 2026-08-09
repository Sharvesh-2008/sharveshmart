package com.digitalmarketplace.service.payment;

public record PaymentResult(boolean successful, String providerReference) {

    public static PaymentResult success(String providerReference) {
        return new PaymentResult(true, providerReference);
    }

    public static PaymentResult failure(String providerReference) {
        return new PaymentResult(false, providerReference);
    }
}