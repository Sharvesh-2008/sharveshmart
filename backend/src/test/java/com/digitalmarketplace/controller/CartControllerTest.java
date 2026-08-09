package com.digitalmarketplace.controller;

import com.digitalmarketplace.entity.Cart;
import com.digitalmarketplace.entity.CartItem;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    private User user() {
        User user = new User();
        user.setId(2L);
        user.setName("Buyer");
        return user;
    }

    private Cart cart() {
        Cart cart = new Cart();
        cart.setId(5L);
        cart.setUser(user());
        return cart;
    }

    private Product product() {
        Product product = new Product();
        product.setId(3L);
        product.setTitle("Spring Guide");
        product.setPrice(new BigDecimal("19.99"));
        return product;
    }

    private CartItem cartItem(long id, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setCart(cart());
        item.setProduct(product());
        item.setQuantity(quantity);
        item.setAddedAt(LocalDateTime.now());
        return item;
    }

    @Test
    void getCartReturnsCartWithItems() throws Exception {
        when(cartService.getOrCreateCart(2L)).thenReturn(cart());
        when(cartService.listItems(2L)).thenReturn(List.of(cartItem(1L, 2)));

        mockMvc.perform(get("/api/cart").header("X-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.items[0].productTitle").value("Spring Guide"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void getCartWithoutUserHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemReturnsCreated() throws Exception {
        when(cartService.addItem(eq(2L), eq(3L), eq(2))).thenReturn(cartItem(1L, 2));

        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 3,
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productId").value(3))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void addItemWithInvalidQuantityReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 3,
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addItemWithoutUserHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 3,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemReturnsUpdated() throws Exception {
        when(cartService.updateItemQuantity(2L, 3L, 5)).thenReturn(cartItem(1L, 5));

        mockMvc.perform(put("/api/cart/items/3")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void updateItemWhenMissingInCartReturnsNotFound() throws Exception {
        when(cartService.updateItemQuantity(2L, 9L, 1))
                .thenThrow(new com.digitalmarketplace.exception.ResourceNotFoundException("Cart item not found"));

        mockMvc.perform(put("/api/cart/items/9")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeItemReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/cart/items/3").header("X-User-Id", "2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void addItemWhenProductUnavailableReturnsBadRequest() throws Exception {
        when(cartService.addItem(eq(2L), eq(3L), eq(1)))
                .thenThrow(new BusinessException("Product is not available for purchase"));

        mockMvc.perform(post("/api/cart/items")
                        .header("X-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 3,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
