package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderItem;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.PurchaseEntitlement;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.repository.OrderItemRepository;
import com.digitalmarketplace.repository.PurchaseEntitlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseEntitlementServiceTest {

    @Mock
    private PurchaseEntitlementRepository entitlementRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    private PurchaseEntitlementService entitlementService;

    @BeforeEach
    void setUp() {
        entitlementService = new PurchaseEntitlementService(entitlementRepository, orderItemRepository);
    }

    private User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Product product(long id) {
        Product product = new Product();
        product.setId(id);
        return product;
    }

    private Order order(long id, long userId) {
        Order order = new Order();
        order.setId(id);
        order.setUser(user(userId));
        return order;
    }

    private OrderItem item(Order order, Product product) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        return item;
    }

    @Test
    void grantForOrderCreatesEntitlementForEachPaidItem() {
        Order order = order(1L, 5L);
        Product first = product(10L);
        Product second = product(20L);
        when(orderItemRepository.findByOrderId(1L))
                .thenReturn(List.of(item(order, first), item(order, second)));
        when(entitlementRepository.findByUserIdAndProductId(5L, 10L)).thenReturn(Optional.empty());
        when(entitlementRepository.findByUserIdAndProductId(5L, 20L)).thenReturn(Optional.empty());

        entitlementService.grantForOrder(order);

        verify(entitlementRepository, times(2)).save(any(PurchaseEntitlement.class));
    }

    @Test
    void grantForOrderSkipsItemsThatAlreadyHaveEntitlement() {
        Order order = order(1L, 5L);
        Product owned = product(10L);
        Product fresh = product(20L);
        when(orderItemRepository.findByOrderId(1L))
                .thenReturn(List.of(item(order, owned), item(order, fresh)));
        when(entitlementRepository.findByUserIdAndProductId(5L, 10L))
                .thenReturn(Optional.of(new PurchaseEntitlement()));
        when(entitlementRepository.findByUserIdAndProductId(5L, 20L)).thenReturn(Optional.empty());

        entitlementService.grantForOrder(order);

        verify(entitlementRepository, times(1)).save(any(PurchaseEntitlement.class));
    }

    @Test
    void hasAccessReturnsTrueOnlyWhenEntitlementExists() {
        when(entitlementRepository.findByUserIdAndProductId(5L, 10L))
                .thenReturn(Optional.of(new PurchaseEntitlement()));
        when(entitlementRepository.findByUserIdAndProductId(5L, 20L))
                .thenReturn(Optional.empty());

        assertTrue(entitlementService.hasAccess(5L, 10L));
        assertFalse(entitlementService.hasAccess(5L, 20L));
    }

    @Test
    void listForUserReturnsRepositoryResults() {
        when(entitlementRepository.findByUserId(5L))
                .thenReturn(List.of(new PurchaseEntitlement(), new PurchaseEntitlement()));

        List<PurchaseEntitlement> result = entitlementService.listForUser(5L);

        assertEquals(2, result.size());
        verify(entitlementRepository, never()).existsById(anyLong());
    }
}