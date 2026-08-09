package com.digitalmarketplace.service.payment;

import com.digitalmarketplace.entity.Order;

import java.math.BigDecimal;

public record PaymentRequest(Order order, BigDecimal amount) {
}