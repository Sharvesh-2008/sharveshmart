package com.digitalmarketplace.repository;

import com.digitalmarketplace.entity.PurchaseEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseEntitlementRepository extends JpaRepository<PurchaseEntitlement, Long> {

    List<PurchaseEntitlement> findByUserId(Long userId);

    List<PurchaseEntitlement> findByProductId(Long productId);

    Optional<PurchaseEntitlement> findByUserIdAndProductId(Long userId, Long productId);

    List<PurchaseEntitlement> findByOrderId(Long orderId);
}