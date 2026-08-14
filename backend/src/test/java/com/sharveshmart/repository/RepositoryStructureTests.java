package com.sharveshmart.repository;

import com.sharveshmart.entity.Cart;
import com.sharveshmart.entity.CartItem;
import com.sharveshmart.entity.Category;
import com.sharveshmart.entity.Order;
import com.sharveshmart.entity.OrderItem;
import com.sharveshmart.entity.OrderStatus;
import com.sharveshmart.entity.Payment;
import com.sharveshmart.entity.Product;
import com.sharveshmart.entity.ProductFile;
import com.sharveshmart.entity.ProductStatus;
import com.sharveshmart.entity.PurchaseEntitlement;
import com.sharveshmart.entity.Review;
import com.sharveshmart.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryStructureTests {

    @Test
    void eachRepositoryExtendsJpaRepositoryWithMatchingIdType() {
        assertGenericContract(UserRepository.class, User.class);
        assertGenericContract(CategoryRepository.class, Category.class);
        assertGenericContract(ProductRepository.class, Product.class);
        assertGenericContract(ProductFileRepository.class, ProductFile.class);
        assertGenericContract(CartRepository.class, Cart.class);
        assertGenericContract(CartItemRepository.class, CartItem.class);
        assertGenericContract(OrderRepository.class, Order.class);
        assertGenericContract(OrderItemRepository.class, OrderItem.class);
        assertGenericContract(PaymentRepository.class, Payment.class);
        assertGenericContract(ReviewRepository.class, Review.class);
        assertGenericContract(PurchaseEntitlementRepository.class, PurchaseEntitlement.class);
    }

    @Test
    void declaredDerivedQueryMethodsResolveToExpectedSignatures() throws NoSuchMethodException {
        assertMethod(UserRepository.class, User.class, Optional.class, "findByEmail", String.class);

        assertMethod(ProductRepository.class, Product.class, List.class, "findBySellerId", Long.class);
        assertMethod(ProductRepository.class, Product.class, List.class, "findByCategoryId", Long.class);
        assertMethod(ProductRepository.class, Product.class, List.class, "findByStatus", ProductStatus.class);
        assertMethod(ProductRepository.class, Product.class, Optional.class, "findByIdAndSellerId", Long.class, Long.class);

        assertMethod(ProductFileRepository.class, ProductFile.class, List.class, "findByProductId", Long.class);

        assertMethod(CartRepository.class, Cart.class, Optional.class, "findByUserId", Long.class);

        assertMethod(CartItemRepository.class, CartItem.class, List.class, "findByCartId", Long.class);
        assertMethod(CartItemRepository.class, CartItem.class, Optional.class, "findByCartIdAndProductId", Long.class, Long.class);

        assertMethod(OrderRepository.class, Order.class, List.class, "findByUserId", Long.class);
        assertMethod(OrderRepository.class, Order.class, List.class, "findByStatus", OrderStatus.class);

        assertMethod(OrderItemRepository.class, OrderItem.class, List.class, "findByOrderId", Long.class);

        assertMethod(PaymentRepository.class, Payment.class, Optional.class, "findByOrderId", Long.class);

        assertMethod(ReviewRepository.class, Review.class, List.class, "findByProductId", Long.class);

        assertMethod(PurchaseEntitlementRepository.class, PurchaseEntitlement.class, List.class, "findByUserId", Long.class);
        assertMethod(PurchaseEntitlementRepository.class, PurchaseEntitlement.class, List.class, "findByProductId", Long.class);
        assertMethod(PurchaseEntitlementRepository.class, PurchaseEntitlement.class, List.class, "findByOrderId", Long.class);
        assertMethod(PurchaseEntitlementRepository.class, PurchaseEntitlement.class, Optional.class,
                "findByUserIdAndProductId", Long.class, Long.class);
    }

    private static void assertGenericContract(Class<?> repository, Class<?> expectedEntity) {
        assertTrue(JpaRepository.class.isAssignableFrom(repository),
                repository.getSimpleName() + " must extend JpaRepository");

        ParameterizedType jpaType = Arrays.stream(repository.getGenericInterfaces())
                .filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .filter(t -> t.getRawType().equals(JpaRepository.class))
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        repository.getSimpleName() + " must directly extend JpaRepository<Entity, Long>"));

        assertEquals(expectedEntity, jpaType.getActualTypeArguments()[0],
                repository.getSimpleName() + " entity type argument");
        assertEquals(Long.class, jpaType.getActualTypeArguments()[1],
                repository.getSimpleName() + " id type argument");
    }

    private static void assertMethod(Class<?> repository, Class<?> entityType, Class<?> rawReturnType,
                                     String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = repository.getMethod(name, parameterTypes);
        assertNotNull(method, repository.getSimpleName() + "." + name + " must be declared");
        assertEquals(rawReturnType, method.getReturnType(),
                repository.getSimpleName() + "." + name + " raw return type");

        if (rawReturnType.equals(List.class) || rawReturnType.equals(Optional.class)) {
            ParameterizedType generics = (ParameterizedType) method.getGenericReturnType();
            assertEquals(entityType, generics.getActualTypeArguments()[0],
                    repository.getSimpleName() + "." + name + " element type");
        }
    }
}