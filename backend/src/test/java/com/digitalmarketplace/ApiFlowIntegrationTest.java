package com.digitalmarketplace;

import com.digitalmarketplace.entity.Category;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.ProductFile;
import com.digitalmarketplace.repository.CategoryRepository;
import com.digitalmarketplace.repository.ProductFileRepository;
import com.digitalmarketplace.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductFileRepository productFileRepository;

    @Test
    void buyerCanPublishBrowsePurchaseAndReviewProduct() throws Exception {
        long sellerId = registerUser("Seller One", "seller-" + unique() + "@example.com", "SELLER");
        long buyerId = registerUser("Buyer One", "buyer-" + unique() + "@example.com", "USER");

        long categoryId = createCategory("Ebooks-" + unique());

        long productId = createProduct(sellerId, categoryId);
        submitProduct(sellerId, productId);
        approveProduct(productId);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(productId))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));

        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Guide"));

        mockMvc.perform(get("/api/library/products/" + productId + "/download")
                        .header("X-User-Id", String.valueOf(buyerId)))
                .andExpect(status().isForbidden());

        addToCart(buyerId, productId);
        long orderId = checkout(buyerId);
        pay(orderId, buyerId);

        mockMvc.perform(get("/api/library").header("X-User-Id", String.valueOf(buyerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(productId));

        seedProductFile(productId);

        mockMvc.perform(get("/api/library/products/" + productId + "/download")
                        .header("X-User-Id", String.valueOf(buyerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.fileName").value("spring-guide.pdf"));

        mockMvc.perform(post("/api/products/" + productId + "/reviews")
                        .header("X-User-Id", String.valueOf(buyerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rating": 5,
                                  "comment": "Excellent guide"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(get("/api/products/" + productId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("Buyer One"))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    private String unique() {
        return Long.toString(System.nanoTime());
    }

    private long registerUser(String name, String email, String role) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"email\":\"" + email
                + "\",\"password\":\"password123\",\"role\":\"" + role + "\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return readId(result);
    }

    private long createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Digital content");
        return categoryRepository.save(category).getId();
    }

    private long createProduct(long sellerId, long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("X-User-Id", String.valueOf(sellerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title": "Spring Guide",
                                  "description": "A comprehensive book",
                                  "price": 19.99
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return readId(result);
    }

    private void submitProduct(long sellerId, long productId) throws Exception {
        mockMvc.perform(patch("/api/products/" + productId + "/submit")
                        .header("X-User-Id", String.valueOf(sellerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    private void approveProduct(long productId) throws Exception {
        mockMvc.perform(post("/api/admin/products/" + productId + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    private void addToCart(long buyerId, long productId) throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", String.valueOf(buyerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated());
    }

    private long checkout(long buyerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/orders/checkout")
                        .header("X-User-Id", String.valueOf(buyerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return readId(result);
    }

    private void pay(long orderId, long buyerId) throws Exception {
        mockMvc.perform(post("/api/orders/" + orderId + "/pay")
                        .header("X-User-Id", String.valueOf(buyerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    private void seedProductFile(long productId) {
        Product product = productRepository.findById(productId).orElseThrow();
        ProductFile file = new ProductFile();
        file.setProduct(product);
        file.setFileName("spring-guide.pdf");
        file.setStorageReference("s3://bucket/" + productId + "-" + unique());
        file.setFileType("application/pdf");
        file.setFileSize(2048L);
        productFileRepository.save(file);
    }

    private long readId(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }
}
