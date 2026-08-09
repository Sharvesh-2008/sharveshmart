package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Cart;
import com.digitalmarketplace.entity.CartItem;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.repository.CartItemRepository;
import com.digitalmarketplace.repository.CartRepository;
import com.digitalmarketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductService productService;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, cartItemRepository, userRepository, productService);
    }

    private User user(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Cart cart(long id, long userId) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setUser(user(userId));
        return cart;
    }

    private Product approvedProduct(long id) {
        Product product = new Product();
        product.setId(id);
        product.setPrice(new BigDecimal("9.99"));
        return product;
    }

    @Test
    void getOrCreateCartReturnsExistingCart() {
        Cart cart = cart(1L, 5L);
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));

        assertEquals(cart, cartService.getOrCreateCart(5L));
    }

    @Test
    void getOrCreateCartCreatesCartOnFirstUse() {
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user(5L)));
        when(cartRepository.save(any(Cart.class))).then(returnsFirstArg());

        Cart result = cartService.getOrCreateCart(5L);

        assertEquals(5L, result.getUser().getId());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void getOrCreateCartThrowsWhenUserMissing() {
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> cartService.getOrCreateCart(99L));
    }

    @Test
    void addItemCreatesNewCartItemForApprovedProduct() {
        Product product = approvedProduct(10L);
        when(productService.getApprovedProduct(10L)).thenReturn(product);
        Cart cart = cart(1L, 5L);
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).then(returnsFirstArg());

        CartItem result = cartService.addItem(5L, 10L, 2);

        assertEquals(product, result.getProduct());
        assertEquals(2, result.getQuantity());
        assertEquals(cart, result.getCart());
    }

    @Test
    void addItemIncrementsQuantityOfExistingCartItem() {
        Product product = approvedProduct(10L);
        when(productService.getApprovedProduct(10L)).thenReturn(product);
        Cart cart = cart(1L, 5L);
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));

        CartItem existing = new CartItem();
        existing.setCart(cart);
        existing.setProduct(product);
        existing.setQuantity(2);
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(existing)).then(returnsFirstArg());

        CartItem result = cartService.addItem(5L, 10L, 3);

        assertEquals(5, result.getQuantity());
    }

    @Test
    void addItemRejectsProductThatIsNotApproved() {
        when(productService.getApprovedProduct(99L))
                .thenThrow(new BusinessException("Product is not available for purchase"));

        assertThrows(BusinessException.class, () -> cartService.addItem(5L, 99L, 1));
    }

    @Test
    void updateItemQuantityThrowsWhenQuantityBelowOne() {
        assertThrows(BusinessException.class, () -> cartService.updateItemQuantity(5L, 10L, 0));
    }

    @Test
    void updateItemQuantitySetsQuantityAndSaves() {
        Cart cart = cart(3L, 5L);
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(approvedProduct(10L));
        when(cartItemRepository.findByCartIdAndProductId(3L, 10L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(item)).then(returnsFirstArg());

        CartItem result = cartService.updateItemQuantity(5L, 10L, 4);

        assertEquals(4, result.getQuantity());
    }

    @Test
    void updateItemQuantityThrowsWhenItemMissing() {
        Cart cart = cart(3L, 5L);
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(3L, 10L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> cartService.updateItemQuantity(5L, 10L, 2));
    }

    @Test
    void removeItemDeletesExistingItem() {
        Cart cart = cart(3L, 5L);
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));

        CartItem item = new CartItem();
        item.setCart(cart);
        when(cartItemRepository.findByCartIdAndProductId(3L, 10L)).thenReturn(Optional.of(item));

        cartService.removeItem(5L, 10L);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void removeItemThrowsWhenItemMissing() {
        Cart cart = cart(3L, 5L);
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(3L, 10L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> cartService.removeItem(5L, 10L));
    }

    @Test
    void listItemsReturnsCartItems() {
        Cart cart = cart(8L, 5L);
        when(cartRepository.findByUserId(5L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(8L)).thenReturn(List.of(new CartItem()));

        assertEquals(1, cartService.listItems(5L).size());
    }
}