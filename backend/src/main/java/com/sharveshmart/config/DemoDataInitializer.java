package com.sharveshmart.config;

import com.sharveshmart.entity.Category;
import com.sharveshmart.entity.Product;
import com.sharveshmart.entity.ProductFile;
import com.sharveshmart.entity.ProductStatus;
import com.sharveshmart.entity.User;
import com.sharveshmart.entity.UserRole;
import com.sharveshmart.repository.CategoryRepository;
import com.sharveshmart.repository.ProductFileRepository;
import com.sharveshmart.repository.ProductRepository;
import com.sharveshmart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
    private static final String DEMO_PASSWORD = "DemoPass123!";

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductFileRepository productFileRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DemoDataInitializer(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               ProductFileRepository productFileRepository,
                               BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productFileRepository = productFileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedDemoData();
    }

    private void seedDemoData() {
        if (categoryRepository.count() > 0) {
            log.info("Demo data already present; skipping.");
            return;
        }

        User admin = findOrCreateUser("Demo Admin", "admin@demo.com", UserRole.ADMIN);
        User seller = findOrCreateUser("Demo Seller", "seller@demo.com", UserRole.SELLER);
        findOrCreateUser("Demo Buyer", "buyer@demo.com", UserRole.USER);

        Category ebooks = findOrCreateCategory("Ebooks", "Digital books, guides and written content");
        Category software = findOrCreateCategory("Software", "Desktop and web applications");
        Category templates = findOrCreateCategory("Digital Templates", "Resume, CV, presentation and document templates");
        Category designAssets = findOrCreateCategory("Design Assets", "Icons, fonts, UI kits and graphics");
        Category courses = findOrCreateCategory("Courses", "Paid online courses and training material");

        createApprovedProduct(seller, ebooks, "Spring Boot Essentials",
                "A practical introduction to building REST APIs with Spring Boot.",
                new BigDecimal("19.99"), "spring-boot-essentials.pdf", "application/pdf");

        createApprovedProduct(seller, designAssets, "Vector Logo Pack",
                "A collection of editable vector logo assets for your next project.",
                new BigDecimal("9.99"), "vector-logo-pack.zip", "application/zip");

        createApprovedProduct(seller, templates, "Resume Template Kit",
                "Modern resume templates that work with Word, Google Docs and PDF.",
                new BigDecimal("14.99"), null, null);

        createApprovedProduct(seller, software, "Invoice Manager Pro",
                "Desktop invoicing tool with PDF export and simple reporting.",
                new BigDecimal("39.99"), null, null);

        createApprovedProduct(seller, courses, "Introduction to Digital Commerce",
                "A beginner course covering the fundamentals of selling digital goods.",
                new BigDecimal("49.99"), null, null);

        log.info("Demo data seeded: {} (ADMIN), {} (SELLER), demo buyer, {} categories and {} approved products.",
                admin.getEmail(), seller.getEmail(), categoryRepository.count(), productRepository.count());
    }

    private User findOrCreateUser(String name, String email, UserRole role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            user.setRole(role);
            return userRepository.save(user);
        });
    }

    private Category findOrCreateCategory(String name, String description) {
        return categoryRepository.findAll().stream()
                .filter(category -> name.equals(category.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(name);
                    category.setDescription(description);
                    return categoryRepository.save(category);
                });
    }

    private Product createApprovedProduct(User seller, Category category, String title, String description,
                                          BigDecimal price, String fileName, String fileType) {
        Product product = productRepository.findAll().stream()
                .filter(existing -> title.equals(existing.getTitle()))
                .findFirst()
                .orElseGet(() -> {
                    Product newProduct = new Product();
                    newProduct.setSeller(seller);
                    newProduct.setCategory(category);
                    newProduct.setTitle(title);
                    newProduct.setDescription(description);
                    newProduct.setPrice(price);
                    newProduct.setStatus(ProductStatus.APPROVED);
                    return productRepository.save(newProduct);
                });

        if (fileName != null) {
            boolean fileExists = productFileRepository.findAll().stream()
                    .anyMatch(file -> file.getProduct().getId().equals(product.getId()));
            if (!fileExists) {
                ProductFile file = new ProductFile();
                file.setProduct(product);
                file.setFileName(fileName);
                file.setStorageReference("demo/" + product.getId() + "-" + fileName);
                file.setFileType(fileType);
                file.setFileSize(1024L);
                productFileRepository.save(file);
            }
        }
        return product;
    }
}