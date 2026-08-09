package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderItem;
import com.digitalmarketplace.entity.PurchaseEntitlement;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.repository.OrderItemRepository;
import com.digitalmarketplace.repository.PurchaseEntitlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PurchaseEntitlementService {

    private final PurchaseEntitlementRepository entitlementRepository;
    private final OrderItemRepository orderItemRepository;

    public PurchaseEntitlementService(PurchaseEntitlementRepository entitlementRepository,
                                      OrderItemRepository orderItemRepository) {
        this.entitlementRepository = entitlementRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public void grantForOrder(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            if (entitlementRepository.findByUserIdAndProductId(
                    order.getUser().getId(), item.getProduct().getId()).isPresent()) {
                continue;
            }
            PurchaseEntitlement entitlement = new PurchaseEntitlement();
            entitlement.setUser(order.getUser());
            entitlement.setProduct(item.getProduct());
            entitlement.setOrder(order);
            entitlementRepository.save(entitlement);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasAccess(Long userId, Long productId) {
        return entitlementRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }

    @Transactional(readOnly = true)
    public List<PurchaseEntitlement> listForUser(Long userId) {
        return entitlementRepository.findByUserId(userId);
    }
}