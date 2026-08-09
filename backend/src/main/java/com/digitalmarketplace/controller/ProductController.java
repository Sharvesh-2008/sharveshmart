package com.digitalmarketplace.controller;

import com.digitalmarketplace.dto.ProductCreateRequest;
import com.digitalmarketplace.dto.ProductResponse;
import com.digitalmarketplace.dto.ProductUpdateRequest;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Tag(name = "Products", description = "Product catalog and seller product management")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/products")
    public List<ProductResponse> listProducts(
            @RequestParam(required = false) @Positive(message = "Category id must be positive") Long categoryId) {
        List<Product> products = categoryId == null
                ? productService.listApproved()
                : productService.listApprovedByCategory(categoryId);
        return products.stream().map(ProductResponse::from).toList();
    }

    @GetMapping("/api/products/{productId}")
    public ProductResponse getProduct(@PathVariable @Positive(message = "Product id must be positive") Long productId) {
        return ProductResponse.from(productService.getApprovedProduct(productId));
    }

    @GetMapping("/api/sellers/{sellerId}/products")
    public List<ProductResponse> listProductsBySeller(
            @PathVariable @Positive(message = "Seller id must be positive") Long sellerId) {
        return productService.listBySeller(sellerId).stream().map(ProductResponse::from).toList();
    }

    @Operation(summary = "Create a new draft product")
    @PostMapping("/api/products")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @Valid @RequestBody ProductCreateRequest request) {
        Product product = productService.createProduct(
                userId, request.categoryId(), request.title(), request.description(), request.price());
        ProductResponse response = ProductResponse.from(product);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/api/products/" + product.getId())
                .body(response);
    }

    @PutMapping("/api/products/{productId}")
    public ProductResponse updateProduct(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Product id must be positive") Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {
        Product product = productService.updateProduct(
                userId, productId, request.categoryId(), request.title(), request.description(), request.price());
        return ProductResponse.from(product);
    }

    @DeleteMapping("/api/products/{productId}")
    public ResponseEntity<Void> archiveProduct(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Product id must be positive") Long productId) {
        productService.archiveProduct(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/products/{productId}/submit")
    public ProductResponse submitProduct(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Product id must be positive") Long productId) {
        return ProductResponse.from(productService.submitForApproval(userId, productId));
    }
}
