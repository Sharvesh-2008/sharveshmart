package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderStatus;
import com.digitalmarketplace.entity.Payment;
import com.digitalmarketplace.entity.PaymentStatus;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.repository.OrderRepository;
import com.digitalmarketplace.repository.PaymentRepository;
import com.digitalmarketplace.service.payment.PaymentProcessor;
import com.digitalmarketplace.service.payment.PaymentRequest;
import com.digitalmarketplace.service.payment.PaymentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentProcessor paymentProcessor;

    @Mock
    private PurchaseEntitlementService purchaseEntitlementService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, orderRepository,
                paymentProcessor, purchaseEntitlementService);
    }

    private User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Order pendingOrder() {
        Order order = new Order();
        order.setId(10L);
        order.setUser(user(5L));
        order.setTotalAmount(new BigDecimal("30.00"));
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    @Test
    void processOrderPaymentThrowsWhenOrderMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> paymentService.processOrderPayment(99L));
    }

    @Test
    void processOrderPaymentThrowsWhenOrderIsNotPending() {
        Order order = pendingOrder();
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> paymentService.processOrderPayment(10L));
    }

    @Test
    void processOrderPaymentMarksPaidAndGrantsEntitlementsOnSuccess() {
        Order order = pendingOrder();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(paymentProcessor.process(any(PaymentRequest.class)))
                .thenReturn(PaymentResult.success("MOCK-REF-123"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.processOrderPayment(10L);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals("MOCK", result.getMethod());
        assertEquals("MOCK-REF-123", result.getProviderReference());
        assertNotNull(result.getPaidAt());
        assertEquals(0, new BigDecimal("30.00").compareTo(result.getAmount()));
        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(purchaseEntitlementService).grantForOrder(order);
    }

    @Test
    void processOrderPaymentMarksOrderFailedAndGrantsNoEntitlementOnFailure() {
        Order order = pendingOrder();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(paymentProcessor.process(any(PaymentRequest.class)))
                .thenReturn(PaymentResult.failure("ERR-001"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment payment = paymentService.processOrderPayment(10L);

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("ERR-001", payment.getProviderReference());
        assertNull(payment.getPaidAt());
        assertEquals(OrderStatus.FAILED, order.getStatus());
        verify(purchaseEntitlementService, never()).grantForOrder(order);
    }
}