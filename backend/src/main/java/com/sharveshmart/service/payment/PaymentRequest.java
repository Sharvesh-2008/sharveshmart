package com.sharveshmart.service.payment;

import com.sharveshmart.entity.Order;

import java.math.BigDecimal;

public record PaymentRequest(Order order, BigDecimal amount) {
}