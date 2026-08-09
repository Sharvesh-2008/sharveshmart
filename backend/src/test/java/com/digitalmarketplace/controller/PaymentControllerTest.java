package com.digitalmarketplace.controller;

import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderStatus;
import com.digitalmarketplace.entity.Payment;
import com.digitalmarketplace.entity.PaymentStatus;
import com.digitalmarketplace.exception.ResourceNotFoundException;
import com.digitalmarketplace.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    private Payment payment() {
        Order order = new Order();
        order.setId(7L);
        order.setStatus(OrderStatus.PENDING);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrder(order);
        payment.setAmount(new BigDecimal("19.99"));
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setMethod("MOCK");
        payment.setProviderReference("ref-123");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setPaidAt(LocalDateTime.now());
        return payment;
    }

    @Test
    void payReturnsPaymentResponse() throws Exception {
        when(paymentService.processOrderPayment(7L)).thenReturn(payment());

        mockMvc.perform(post("/api/orders/7/pay").header("X-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(7))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(19.99))
                .andExpect(jsonPath("$.providerReference").value("ref-123"));
    }

    @Test
    void payWhenOrderNotFoundReturnsNotFound() throws Exception {
        when(paymentService.processOrderPayment(99L))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(post("/api/orders/99/pay").header("X-User-Id", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void payWithoutUserHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/orders/7/pay"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payWithInvalidOrderIdReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/orders/0/pay").header("X-User-Id", "2"))
                .andExpect(status().isBadRequest());
    }
}
