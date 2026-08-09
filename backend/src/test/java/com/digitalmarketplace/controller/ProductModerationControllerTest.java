package com.digitalmarketplace.controller;

import com.digitalmarketplace.entity.Category;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.ProductStatus;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductModerationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private Product product(long id, ProductStatus status) {
        User seller = new User();
        seller.setId(2L);
        seller.setName("Seller");
        Category category = new Category();
        category.setId(1L);
        category.setName("Ebooks");

        Product product = new Product();
        product.setId(id);
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle("Spring Guide");
        product.setPrice(new BigDecimal("19.99"));
        product.setStatus(status);
        return product;
    }

    @Test
    void listPendingReturnsPendingProducts() throws Exception {
        when(productService.listPending()).thenReturn(List.of(product(1L, ProductStatus.PENDING_APPROVAL)));

        mockMvc.perform(get("/api/admin/products/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING_APPROVAL"));
    }

    @Test
    void approveProductReturnsApproved() throws Exception {
        when(productService.approveProduct(1L)).thenReturn(product(1L, ProductStatus.APPROVED));

        mockMvc.perform(post("/api/admin/products/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void rejectProductReturnsRejected() throws Exception {
        when(productService.rejectProduct(1L)).thenReturn(product(1L, ProductStatus.REJECTED));

        mockMvc.perform(post("/api/admin/products/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
}
