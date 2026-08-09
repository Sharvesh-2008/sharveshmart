package com.digitalmarketplace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityDefaultsTests {

    @Test
    void allEntitiesAreAnnotatedWithEntityAndExpectedTableNames() {
        assertEquals("users", tableNameOf(User.class));
        assertEquals("categories", tableNameOf(Category.class));
        assertEquals("products", tableNameOf(Product.class));
        assertEquals("product_files", tableNameOf(ProductFile.class));
        assertEquals("carts", tableNameOf(Cart.class));
        assertEquals("cart_items", tableNameOf(CartItem.class));
        assertEquals("orders", tableNameOf(Order.class));
        assertEquals("order_items", tableNameOf(OrderItem.class));
        assertEquals("payments", tableNameOf(Payment.class));
        assertEquals("reviews", tableNameOf(Review.class));
        assertEquals("purchase_entitlements", tableNameOf(PurchaseEntitlement.class));
    }

    @Test
    void everyClassInEntityPackageIsAnEntity() {
        assertNotNull(User.class.getAnnotation(Entity.class));
        assertNotNull(Category.class.getAnnotation(Entity.class));
        assertNotNull(Product.class.getAnnotation(Entity.class));
        assertNotNull(ProductFile.class.getAnnotation(Entity.class));
        assertNotNull(Cart.class.getAnnotation(Entity.class));
        assertNotNull(CartItem.class.getAnnotation(Entity.class));
        assertNotNull(Order.class.getAnnotation(Entity.class));
        assertNotNull(OrderItem.class.getAnnotation(Entity.class));
        assertNotNull(Payment.class.getAnnotation(Entity.class));
        assertNotNull(Review.class.getAnnotation(Entity.class));
        assertNotNull(PurchaseEntitlement.class.getAnnotation(Entity.class));
    }

    @Test
    void enumValuesMatchSchema() {
        assertEquals(Arrays.asList(UserRole.USER, UserRole.SELLER, UserRole.ADMIN),
                Arrays.asList(UserRole.values()));
        assertEquals(Arrays.asList(ProductStatus.DRAFT, ProductStatus.PENDING_APPROVAL,
                        ProductStatus.APPROVED, ProductStatus.REJECTED, ProductStatus.ARCHIVED),
                Arrays.asList(ProductStatus.values()));
        assertEquals(Arrays.asList(OrderStatus.PENDING, OrderStatus.PAID,
                        OrderStatus.FAILED, OrderStatus.CANCELLED),
                Arrays.asList(OrderStatus.values()));
        assertEquals(Arrays.asList(PaymentStatus.PENDING, PaymentStatus.SUCCESS, PaymentStatus.FAILED),
                Arrays.asList(PaymentStatus.values()));
    }

    @Test
    void fieldDefaultsPreserveDocumentedApplicationDefaults() {
        assertEquals(UserRole.USER, new User().getRole());
        assertEquals(ProductStatus.DRAFT, new Product().getStatus());
        assertEquals(OrderStatus.PENDING, new Order().getStatus());
        assertEquals(PaymentStatus.PENDING, new Payment().getStatus());
        assertEquals("MOCK", new Payment().getMethod());
        assertEquals(1, new CartItem().getQuantity());
    }

    @Test
    void timestampsDefaultToNowAndPaidAtStaysNull() {
        assertNotNull(new User().getCreatedAt());
        assertNotNull(new Product().getCreatedAt());
        assertNotNull(new Product().getUpdatedAt());
        assertNotNull(new ProductFile().getCreatedAt());
        assertNotNull(new Order().getCreatedAt());
        assertNotNull(new CartItem().getAddedAt());
        assertNotNull(new Payment().getCreatedAt());
        assertNull(new Payment().getPaidAt());
        assertNotNull(new Review().getCreatedAt());
        assertNotNull(new PurchaseEntitlement().getGrantedAt());
    }

    @Test
    void expectedUniqueJoinColumnsAreUnique() {
        assertTrue(isUniqueJoinColumn(Cart.class, "user"));
        assertTrue(isUniqueJoinColumn(Payment.class, "order"));
    }

    @Test
    void expectedSingleColumnsAreUnique() {
        assertTrue(isColumnUnique(User.class, "email"));
        assertTrue(isColumnUnique(Category.class, "name"));
        assertTrue(isColumnUnique(ProductFile.class, "storageReference"));
    }

    @Test
    void expectedCompositeUniqueConstraintsExist() {
        assertTrue(hasUniqueConstraint(CartItem.class, "uk_cart_items_cart_product", "cart_id", "product_id"));
        assertTrue(hasUniqueConstraint(Review.class, "uk_reviews_user_product", "user_id", "product_id"));
        assertTrue(hasUniqueConstraint(PurchaseEntitlement.class,
                "uk_purchase_entitlements_user_product", "user_id", "product_id"));
    }

    @Test
    void expectedIndexesExist() {
        assertTrue(hasIndex(Product.class, "idx_products_seller_id", "seller_id"));
        assertTrue(hasIndex(Product.class, "idx_products_category_id", "category_id"));
        assertTrue(hasIndex(Product.class, "idx_products_status", "status"));
        assertTrue(hasIndex(Order.class, "idx_orders_user_id", "user_id"));
        assertTrue(hasIndex(Order.class, "idx_orders_status", "status"));
        assertTrue(hasIndex(OrderItem.class, "idx_order_items_order_id", "order_id"));
        assertTrue(hasIndex(Review.class, "idx_reviews_product_id", "product_id"));
        assertTrue(hasIndex(PurchaseEntitlement.class, "idx_purchase_entitlements_product_id", "product_id"));
        assertTrue(hasIndex(PurchaseEntitlement.class, "idx_purchase_entitlements_order_id", "order_id"));
    }

    private static String tableNameOf(Class<?> type) {
        Table table = type.getAnnotation(Table.class);
        assertNotNull(table, type.getSimpleName() + " should have a @Table annotation");
        return table.name();
    }

    private static boolean isColumnUnique(Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            Column column = field.getAnnotation(Column.class);
            return column != null && column.unique();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isUniqueJoinColumn(Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            return joinColumn != null && joinColumn.unique();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean hasUniqueConstraint(Class<?> type, String name, String... columnNames) {
        Table table = type.getAnnotation(Table.class);
        if (table == null) {
            return false;
        }
        for (UniqueConstraint constraint : table.uniqueConstraints()) {
            if (constraint.name().equals(name)
                    && Arrays.equals(constraint.columnNames(), columnNames)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasIndex(Class<?> type, String name, String column) {
        Table table = type.getAnnotation(Table.class);
        if (table == null) {
            return false;
        }
        return Arrays.stream(table.indexes())
                .anyMatch(index -> index.name().equals(name) && index.columnList().equals(column));
    }
}