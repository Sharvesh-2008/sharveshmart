package com.digitalmarketplace.controller;

import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.ProductFile;
import com.digitalmarketplace.entity.PurchaseEntitlement;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.entity.UserRole;
import com.digitalmarketplace.service.PurchaseEntitlementService;
import com.digitalmarketplace.support.SecurityTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DigitalLibraryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityTestSupport.class)
class DigitalLibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PurchaseEntitlementService entitlementService;

    @BeforeEach
    void setUp() {
        SecurityTestSupport.authenticate(2L, "buyer@example.com", UserRole.USER);
    }

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    private Product product() {
        Product product = new Product();
        product.setId(3L);
        product.setTitle("Spring Guide");
        return product;
    }

    private ProductFile productFile() {
        ProductFile file = new ProductFile();
        file.setId(1L);
        file.setProduct(product());
        file.setFileName("spring-guide.pdf");
        file.setFileType("application/pdf");
        file.setFileSize(2048L);
        return file;
    }

    private PurchaseEntitlement entitlement() {
        User user = new User();
        user.setId(2L);

        Order order = new Order();
        order.setId(7L);

        PurchaseEntitlement entitlement = new PurchaseEntitlement();
        entitlement.setId(9L);
        entitlement.setUser(user);
        entitlement.setProduct(product());
        entitlement.setOrder(order);
        entitlement.setGrantedAt(LocalDateTime.now());
        return entitlement;
    }

    @Test
    void listLibraryReturnsItems() throws Exception {
        when(entitlementService.listForUser(2L)).thenReturn(List.of(entitlement()));

        mockMvc.perform(get("/api/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].productId").value(3))
                .andExpect(jsonPath("$[0].productTitle").value("Spring Guide"))
                .andExpect(jsonPath("$[0].orderId").value(7));
    }

    @Test
    void downloadReturnsAuthorization() throws Exception {
        when(entitlementService.getAuthorizedFile(2L, 3L))
                .thenReturn(Optional.of(productFile()));

        mockMvc.perform(get("/api/library/products/3/download"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(3))
                .andExpect(jsonPath("$.productTitle").value("Spring Guide"))
                .andExpect(jsonPath("$.fileName").value("spring-guide.pdf"))
                .andExpect(jsonPath("$.fileType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(2048));
    }

    @Test
    void downloadWhenNotEntitledReturnsForbidden() throws Exception {
        when(entitlementService.getAuthorizedFile(2L, 3L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/library/products/3/download"))
                .andExpect(status().isForbidden());
    }

    @Test
    void downloadWithInvalidProductIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/library/products/0/download"))
                .andExpect(status().isBadRequest());
    }
}
