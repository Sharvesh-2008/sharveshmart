package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Cart;
import com.digitalmarketplace.entity.CartItem;
import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderItem;
import com.digitalmarketplace.entity.OrderStatus;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.repository.CartRepository;
import com.digitalmarketplace.repository.OrderItemRepository;
import com.digitalmarketplace.repository.OrderRepository;
import com.digitalmarketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderItemRepository,
                cartRepository, userRepository, productService);
    }

    private User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Product product(long id, String price) {
        Product product = new Product();
        product.setId(id);
        product.setPrice(new BigDecimal(price));
        return product;
    }

    private CartItem cartItem(Product product, int quantity) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    private Cart cart(long userId, CartItem... items) {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(user(userId));
        cart.setItems(new ArrayList<>(List.of(items)));
        return cart;
    }

    @Test
    void createOrderFromCartThrowsWhenCartIsEmpty() {
        User buyer = user(5L);
        Cart empty = cart(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(buyer));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(empty));

        assertThrows(BusinessException.class, () -> orderService.createOrderFromCart(5L));
    }

    @Test
    void createOrderFromCartThrowsWhenCartMissing() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> orderService.createOrderFromCart(5L));
    }

    @Test
    void createOrderComputesTotalFromServerSidePricesAndSnapshotsUnitPrices() {
        Product book = product(10L, "10.00");
        Product template = product(20L, "5.50");
        when(productService.getApprovedProduct(10L)).thenReturn(book);
        when(productService.getApprovedProduct(20L)).thenReturn(template);

        Cart cart = cart(5L, cartItem(book, 3), cartItem(template, 2));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrderFromCart(5L);

        assertEquals(0, new BigDecimal("41.00").compareTo(result.getTotalAmount()));
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(5L, result.getUser().getId());
        assertEquals(2, result.getItems().size());

        OrderItem first = result.getItems().get(0);
        assertEquals(0, new BigDecimal("10.00").compareTo(first.getUnitPrice()));
        assertEquals(0, new BigDecimal("5.50").compareTo(result.getItems().get(1).getUnitPrice()));
        assertEquals(3, first.getQuantity());
        assertEquals(result, first.getOrder());

        assertEquals(0, cart.getItems().size());
        verify(orderItemRepository, never()).save(any(OrderItem.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void createOrderFromCartRejectsUnapprovedProduct() {
        when(productService.getApprovedProduct(99L))
                .thenThrow(new BusinessException("Product is not available for purchase"));

        Cart cart = cart(5L, cartItem(product(99L, "10.00"), 1));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));

        assertThrows(BusinessException.class, () -> orderService.createOrderFromCart(5L));
    }

    @Test
    void createOrderFromCartRejectsZeroTotal() {
        Product free = product(1L, "0.00");
        when(productService.getApprovedProduct(1L)).thenReturn(free);

        Cart cart = cart(5L, cartItem(free, 1));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));

        assertThrows(BusinessException.class, () -> orderService.createOrderFromCart(5L));
    }

    @Test
    void getOrderThrowsWhenMissing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> orderService.getOrder(1L));
    }

    @Test
    void getOrderReturnsOrder() {
        Order order = new Order();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertEquals(order, orderService.getOrder(1L));
    }

    @Test
    void listOrdersByUserDelegatesToRepository() {
        when(orderRepository.findByUserId(5L)).thenReturn(List.of(new Order()));

        assertEquals(1, orderService.listOrdersByUser(5L).size());
    }

    @Test
    void listOrderItemsDelegatesToRepository() {
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(new OrderItem()));

        assertEquals(1, orderService.listOrderItems(1L).size());
    }
}