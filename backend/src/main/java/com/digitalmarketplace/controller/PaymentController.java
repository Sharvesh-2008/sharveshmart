package com.digitalmarketplace.controller;

import com.digitalmarketplace.dto.PaymentResponse;
import com.digitalmarketplace.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Validated
@Tag(name = "Payments", description = "Order payment processing")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Pay for a pending order")
    @PostMapping("/{orderId}/pay")
    public PaymentResponse pay(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Order id must be positive") Long orderId) {
        return PaymentResponse.from(paymentService.processOrderPayment(orderId));
    }
}
