package com.digitalmarketplace;

import com.digitalmarketplace.entity.Category;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.ProductFile;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.entity.UserRole;
import com.digitalmarketplace.repository.CategoryRepository;
import com.digitalmarketplace.repository.ProductFileRepository;
import com.digitalmarketplace.repository.ProductRepository;
import com.digitalmarketplace.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private record Registered(long id, String email) {
    }

    @Test
    void buyerCanPublishBrowsePurchaseAndReviewProduct() throws Exception {
        Registered seller = registerUser("Seller One", "seller-" + unique() + "@example.com", "SELLER");
        Registered buyer = registerUser("Buyer One", "buyer-" + unique() + "@example.com", "USER");
        String sellerToken = login(seller.email(), "password123");
        String buyerToken = login(buyer.email(), "password123");
        String adminToken = adminToken();

        long categoryId = createCategory("Ebooks-" + unique());

        long productId = createProduct(sellerToken, categoryId);
        submitProduct(sellerToken, productId);
        approveProduct(adminToken, productId);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(productId))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));

        mockMvc.perform(get("/api/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Guide"));

        mockMvc.perform(get("/api/library/products/" + productId + "/download")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());

        addToCart(buyerToken, productId);
        long orderId = checkout(buyerToken);
        pay(buyerToken, orderId);

        mockMvc.perform(get("/api/library").header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(productId));

        seedProductFile(productId);

        mockMvc.perform(get("/api/library/products/" + productId + "/download")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.fileName").value("spring-guide.pdf"));

        mockMvc.perform(post("/api/products/" + productId + "/reviews")
                        .header("Authorization", "Bearer " + buyerToken)
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

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "nobody@example.com",
                                  "password": "wrong"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongRolesAreRejected() throws Exception {
        Registered buyer = registerUser("Buyer Two", "buyer-" + unique() + "@example.com", "USER");
        String buyerToken = login(buyer.email(), "password123");
        long categoryId = createCategory("Ebooks-" + unique());

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": %d,
                                  "title": "Nope",
                                  "price": 1.99
                                }
                                """.formatted(categoryId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/products/1/approve")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void sellerCannotCheckout() throws Exception {
        Registered seller = registerUser("Seller Two", "seller-" + unique() + "@example.com", "SELLER");
        String sellerToken = login(seller.email(), "password123");

        mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + sellerToken))
                .andExpect(status().isForbidden());
    }

    private String unique() {
        return Long.toString(System.nanoTime());
    }

    private Registered registerUser(String name, String email, String role) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"email\":\"" + email
                + "\",\"password\":\"password123\",\"role\":\"" + role + "\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return new Registered(readId(result), email);
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String adminToken() throws Exception {
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail("admin-" + unique() + "@example.com");
        admin.setPasswordHash(passwordEncoder.encode("password123"));
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);
        return login(admin.getEmail(), "password123");
    }

    private long createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription("Digital content");
        return categoryRepository.save(category).getId();
    }

    private long createProduct(String token, long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
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

    private void submitProduct(String token, long productId) throws Exception {
        mockMvc.perform(patch("/api/products/" + productId + "/submit")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
    }

    private void approveProduct(String token, long productId) throws Exception {
        mockMvc.perform(post("/api/admin/products/" + productId + "/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    private void addToCart(String token, long productId) throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(productId)))
                .andExpect(status().isCreated());
    }

    private long checkout(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/orders/checkout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return readId(result);
    }

    private void pay(String token, long orderId) throws Exception {
        mockMvc.perform(post("/api/orders/" + orderId + "/pay")
                        .header("Authorization", "Bearer " + token))
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
