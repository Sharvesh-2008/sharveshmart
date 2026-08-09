package com.digitalmarketplace.repository;

import com.digitalmarketplace.entity.ProductFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductFileRepository extends JpaRepository<ProductFile, Long> {

    List<ProductFile> findByProductId(Long productId);
}