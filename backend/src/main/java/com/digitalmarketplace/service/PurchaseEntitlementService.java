package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderItem;
import com.digitalmarketplace.entity.ProductFile;
import com.digitalmarketplace.entity.PurchaseEntitlement;
import com.digitalmarketplace.repository.OrderItemRepository;
import com.digitalmarketplace.repository.ProductFileRepository;
import com.digitalmarketplace.repository.PurchaseEntitlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PurchaseEntitlementService {

    private final PurchaseEntitlementRepository entitlementRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductFileRepository productFileRepository;

    public PurchaseEntitlementService(PurchaseEntitlementRepository entitlementRepository,
                                      OrderItemRepository orderItemRepository,
                                      ProductFileRepository productFileRepository) {
        this.entitlementRepository = entitlementRepository;
        this.orderItemRepository = orderItemRepository;
        this.productFileRepository = productFileRepository;
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
    public Optional<ProductFile> getAuthorizedFile(Long userId, Long productId) {
        if (entitlementRepository.findByUserIdAndProductId(userId, productId).isEmpty()) {
            return Optional.empty();
        }
        return productFileRepository.findByProductId(productId).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public List<PurchaseEntitlement> listForUser(Long userId) {
        return entitlementRepository.findByUserId(userId);
    }
}