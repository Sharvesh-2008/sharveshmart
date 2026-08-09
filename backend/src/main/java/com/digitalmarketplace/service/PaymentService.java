package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderStatus;
import com.digitalmarketplace.entity.Payment;
import com.digitalmarketplace.entity.PaymentStatus;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.exception.ResourceNotFoundException;
import com.digitalmarketplace.repository.OrderRepository;
import com.digitalmarketplace.repository.PaymentRepository;
import com.digitalmarketplace.service.payment.PaymentProcessor;
import com.digitalmarketplace.service.payment.PaymentRequest;
import com.digitalmarketplace.service.payment.PaymentResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentProcessor paymentProcessor;
    private final PurchaseEntitlementService purchaseEntitlementService;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          PaymentProcessor paymentProcessor,
                          PurchaseEntitlementService purchaseEntitlementService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentProcessor = paymentProcessor;
        this.purchaseEntitlementService = purchaseEntitlementService;
    }

    @Transactional
    public Payment processOrderPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Only pending orders can be paid");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(this::createPayment);
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);

        PaymentResult result = paymentProcessor.process(new PaymentRequest(order, order.getTotalAmount()));

        if (result.successful()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            payment.setProviderReference(result.providerReference());
            paymentRepository.save(payment);
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            purchaseEntitlementService.grantForOrder(order);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setProviderReference(result.providerReference());
            paymentRepository.save(payment);
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
        }
        return payment;
    }

    private Payment createPayment() {
        Payment payment = new Payment();
        payment.setMethod("MOCK");
        return payment;
    }
}