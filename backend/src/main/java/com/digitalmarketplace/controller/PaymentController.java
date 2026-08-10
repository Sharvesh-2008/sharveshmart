package com.digitalmarketplace.controller;

import com.digitalmarketplace.dto.PaymentResponse;
import com.digitalmarketplace.security.UserPrincipal;
import com.digitalmarketplace.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    @PreAuthorize("hasRole('USER')")
    public PaymentResponse pay(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "Order id must be positive") Long orderId) {
        return PaymentResponse.from(paymentService.processOrderPayment(orderId));
    }
}
