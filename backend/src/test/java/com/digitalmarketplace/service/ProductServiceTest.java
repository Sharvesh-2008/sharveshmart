package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Category;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.ProductStatus;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.repository.CategoryRepository;
import com.digitalmarketplace.repository.ProductRepository;
import com.digitalmarketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, userRepository, categoryRepository);
    }

    private User seller(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Category category(long id) {
        Category category = new Category();
        category.setId(id);
        return category;
    }

    private Product ownedProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setSeller(seller(9L));
        product.setCategory(category(2L));
        product.setTitle("Old title");
        product.setDescription("Old description");
        product.setPrice(new BigDecimal("10.00"));
        product.setStatus(ProductStatus.DRAFT);
        return product;
    }

    @Test
    void createProductStartsWithDraftStatus() {
        when(userRepository.findById(9L)).thenReturn(Optional.of(seller(9L)));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category(2L)));
        when(productRepository.save(any(Product.class))).then(returnsFirstArg());

        Product result = productService.createProduct(9L, 2L, "E-book",
                "Learn Java", new BigDecimal("10.00"));

        assertEquals(ProductStatus.DRAFT, result.getStatus());
        assertEquals("E-book", result.getTitle());
        assertEquals(new BigDecimal("10.00"), result.getPrice());
        assertEquals(9L, result.getSeller().getId());
    }

    @Test
    void createProductThrowsWhenSellerNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> productService.createProduct(99L, 2L, "T", "D", new BigDecimal("5")));
    }

    @Test
    void createProductThrowsWhenCategoryNotFound() {
        when(userRepository.findById(9L)).thenReturn(Optional.of(seller(9L)));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> productService.createProduct(9L, 99L, "T", "D", new BigDecimal("5")));
    }

    @Test
    void updateProductRequiresSellerOwnership() {
        when(productRepository.findByIdAndSellerId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> productService.updateProduct(2L, 1L, null, "New", null, null));
    }

    @Test
    void updateProductChangesOnlyProvidedFields() {
        Product product = ownedProduct();
        when(productRepository.findByIdAndSellerId(1L, 9L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).then(returnsFirstArg());
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category(5L)));

        Product result = productService.updateProduct(9L, 1L, 5L,
                "New title", null, new BigDecimal("12.50"));

        assertEquals("New title", result.getTitle());
        assertEquals("Old description", result.getDescription());
        assertEquals(new BigDecimal("12.50"), result.getPrice());
        assertEquals(5L, result.getCategory().getId());
    }

    @Test
    void updateProductWithNullDescriptionKeepsExistingDescription() {
        Product product = ownedProduct();
        product.setDescription("Keep me");
        when(productRepository.findByIdAndSellerId(1L, 9L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).then(returnsFirstArg());

        Product result = productService.updateProduct(9L, 1L, null,
                "New title", null, null);

        assertEquals("Keep me", result.getDescription());
        assertEquals(new BigDecimal("10.00"), result.getPrice());
    }

    @Test
    void archiveProductSetsArchived() {
        Product product = ownedProduct();
        when(productRepository.findByIdAndSellerId(1L, 9L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).then(returnsFirstArg());

        productService.archiveProduct(9L, 1L);

        assertEquals(ProductStatus.ARCHIVED, product.getStatus());
    }

    @Test
    void archiveProductRequiresOwnership() {
        when(productRepository.findByIdAndSellerId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> productService.archiveProduct(2L, 1L));
    }

    @Test
    void submitForApprovalSetsPendingApproval() {
        Product product = ownedProduct();
        when(productRepository.findByIdAndSellerId(1L, 9L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).then(returnsFirstArg());

        productService.submitForApproval(9L, 1L);

        assertEquals(ProductStatus.PENDING_APPROVAL, product.getStatus());
    }

    @Test
    void approveProductSetsApproved() {
        Product product = ownedProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).then(returnsFirstArg());

        Product result = productService.approveProduct(1L);

        assertEquals(ProductStatus.APPROVED, result.getStatus());
    }

    @Test
    void rejectProductSetsRejected() {
        Product product = ownedProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).then(returnsFirstArg());

        productService.rejectProduct(1L);

        assertEquals(ProductStatus.REJECTED, product.getStatus());
    }

    @Test
    void getApprovedProductReturnsProductWhenApproved() {
        Product product = ownedProduct();
        product.setStatus(ProductStatus.APPROVED);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertEquals(product, productService.getApprovedProduct(1L));
    }

    @Test
    void getApprovedProductRejectsNonApprovedProduct() {
        Product product = ownedProduct();
        product.setStatus(ProductStatus.PENDING_APPROVAL);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(BusinessException.class, () -> productService.getApprovedProduct(1L));
    }

    @Test
    void listApprovedUsesRepositoryStatusFilter() {
        when(productRepository.findByStatus(ProductStatus.APPROVED))
                .thenReturn(List.of(ownedProduct()));

        assertEquals(1, productService.listApproved().size());
    }

    @Test
    void listApprovedByCategoryFiltersOnlyApprovedProducts() {
        Product approved = ownedProduct();
        approved.setStatus(ProductStatus.APPROVED);
        Product draft = ownedProduct();
        draft.setStatus(ProductStatus.DRAFT);
        when(productRepository.findByCategoryId(2L)).thenReturn(List.of(approved, draft));

        List<Product> result = productService.listApprovedByCategory(2L);

        assertEquals(1, result.size());
        assertEquals(ProductStatus.APPROVED, result.get(0).getStatus());
    }
}