package com.digitalmarketplace.controller;

import com.digitalmarketplace.dto.ProductResponse;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@Validated
@Tag(name = "Product Moderation", description = "Admin moderation of pending products")
public class ProductModerationController {

    private final ProductService productService;

    public ProductModerationController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/pending")
    public List<ProductResponse> listPending() {
        return productService.listPending().stream().map(ProductResponse::from).toList();
    }

    @PostMapping("/{productId}/approve")
    public ProductResponse approveProduct(
            @PathVariable @Positive(message = "Product id must be positive") Long productId) {
        Product product = productService.approveProduct(productId);
        return ProductResponse.from(product);
    }

    @PostMapping("/{productId}/reject")
    public ProductResponse rejectProduct(
            @PathVariable @Positive(message = "Product id must be positive") Long productId) {
        Product product = productService.rejectProduct(productId);
        return ProductResponse.from(product);
    }
}
