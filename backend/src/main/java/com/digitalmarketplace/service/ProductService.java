package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Category;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.ProductStatus;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.exception.ResourceNotFoundException;
import com.digitalmarketplace.repository.CategoryRepository;
import com.digitalmarketplace.repository.ProductRepository;
import com.digitalmarketplace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Product createProduct(Long sellerId, Long categoryId, String title,
                                 String description, BigDecimal price) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setStatus(ProductStatus.DRAFT);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long sellerId, Long productId, Long categoryId,
                                 String title, String description, BigDecimal price) {
        Product product = requireOwnedProduct(sellerId, productId);
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }
        if (title != null) {
            product.setTitle(title);
        }
        if (description != null) {
            product.setDescription(description);
        }
        if (price != null) {
            product.setPrice(price);
        }
        return productRepository.save(product);
    }

    @Transactional
    public void archiveProduct(Long sellerId, Long productId) {
        Product product = requireOwnedProduct(sellerId, productId);
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);
    }

    @Transactional
    public Product submitForApproval(Long sellerId, Long productId) {
        Product product = requireOwnedProduct(sellerId, productId);
        product.setStatus(ProductStatus.PENDING_APPROVAL);
        return productRepository.save(product);
    }

    @Transactional
    public Product approveProduct(Long productId) {
        Product product = requireProduct(productId);
        product.setStatus(ProductStatus.APPROVED);
        return productRepository.save(product);
    }

    @Transactional
    public Product rejectProduct(Long productId) {
        Product product = requireProduct(productId);
        product.setStatus(ProductStatus.REJECTED);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> listBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    @Transactional(readOnly = true)
    public List<Product> listApproved() {
        return productRepository.findByStatus(ProductStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<Product> listApprovedByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .filter(product -> product.getStatus() == ProductStatus.APPROVED)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Product> listPending() {
        return productRepository.findByStatus(ProductStatus.PENDING_APPROVAL);
    }

    @Transactional(readOnly = true)
    public Product getApprovedProduct(Long productId) {
        Product product = requireProduct(productId);
        if (product.getStatus() != ProductStatus.APPROVED) {
            throw new BusinessException("Product is not available for purchase");
        }
        return product;
    }

    private Product requireOwnedProduct(Long sellerId, Long productId) {
        return productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found or not owned by seller"));
    }

    private Product requireProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
}