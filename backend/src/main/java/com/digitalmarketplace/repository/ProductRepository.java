package com.digitalmarketplace.repository;

import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByStatus(ProductStatus status);

    Optional<Product> findByIdAndSellerId(Long id, Long sellerId);
}