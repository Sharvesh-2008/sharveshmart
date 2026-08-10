package com.digitalmarketplace.controller;

import com.digitalmarketplace.entity.Category;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.ProductStatus;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.entity.UserRole;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.exception.ResourceNotFoundException;
import com.digitalmarketplace.service.ProductService;
import com.digitalmarketplace.support.SecurityTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityTestSupport.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @BeforeEach
    void setUp() {
        SecurityTestSupport.authenticate(2L, "seller@example.com", UserRole.SELLER);
    }

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    private User seller() {
        User user = new User();
        user.setId(2L);
        user.setName("Seller");
        user.setEmail("seller@example.com");
        return user;
    }

    private Category category() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Ebooks");
        return category;
    }

    private Product product(long id, ProductStatus status) {
        Product product = new Product();
        product.setId(id);
        product.setSeller(seller());
        product.setCategory(category());
        product.setTitle("Spring Guide");
        product.setDescription("A book");
        product.setPrice(new BigDecimal("19.99"));
        product.setStatus(status);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    @Test
    void listProductsReturnsApproved() throws Exception {
        when(productService.listApproved()).thenReturn(List.of(product(1L, ProductStatus.APPROVED)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Ebooks"))
                .andExpect(jsonPath("$[0].sellerName").value("Seller"));
    }

    @Test
    void listProductsByCategoryUsesCategory() throws Exception {
        when(productService.listApprovedByCategory(3L))
                .thenReturn(List.of(product(1L, ProductStatus.APPROVED)));

        mockMvc.perform(get("/api/products").param("categoryId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getProductReturnsApprovedProduct() throws Exception {
        when(productService.getApprovedProduct(1L)).thenReturn(product(1L, ProductStatus.APPROVED));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Guide"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getProductWhenNotAvailableReturnsBadRequest() throws Exception {
        when(productService.getApprovedProduct(1L))
                .thenThrow(new BusinessException("Product is not available for purchase"));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductReturnsCreatedWithLocation() throws Exception {
        Product created = product(9L, ProductStatus.DRAFT);
        when(productService.createProduct(eq(2L), eq(1L), eq("Spring Guide"), eq("A book"),
                eq(new BigDecimal("19.99")))).thenReturn(created);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "title": "Spring Guide",
                                  "description": "A book",
                                  "price": 19.99
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/9"))
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void createProductWithInvalidBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "price": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProductReturnsUpdated() throws Exception {
        when(productService.updateProduct(eq(2L), eq(1L), eq(null), eq("New Title"), eq(null), eq(null)))
                .thenReturn(product(1L, ProductStatus.DRAFT));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New Title"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void archiveProductReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void submitProductReturnsSubmitted() throws Exception {
        when(productService.submitForApproval(2L, 1L)).thenReturn(product(1L, ProductStatus.PENDING_APPROVAL));

        mockMvc.perform(patch("/api/products/1/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    @Test
    void listProductsBySellerReturnsProducts() throws Exception {
        when(productService.listBySeller(2L)).thenReturn(List.of(product(1L, ProductStatus.APPROVED)));

        mockMvc.perform(get("/api/sellers/2/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId").value(2));
    }

    @Test
    void createProductWhenSellerMissingReturnsNotFound() throws Exception {
        SecurityTestSupport.authenticate(99L, "missing@example.com", UserRole.SELLER);
        when(productService.createProduct(any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Seller not found"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": 1,
                                  "title": "Spring Guide",
                                  "price": 19.99
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
