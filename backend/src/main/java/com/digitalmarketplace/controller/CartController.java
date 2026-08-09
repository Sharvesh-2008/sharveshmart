package com.digitalmarketplace.controller;

import com.digitalmarketplace.dto.AddCartItemRequest;
import com.digitalmarketplace.dto.CartItemResponse;
import com.digitalmarketplace.dto.CartResponse;
import com.digitalmarketplace.dto.UpdateCartItemRequest;
import com.digitalmarketplace.entity.Cart;
import com.digitalmarketplace.entity.CartItem;
import com.digitalmarketplace.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Validated
@Tag(name = "Cart", description = "Shopping cart operations")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(@RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId) {
        Cart cart = cartService.getOrCreateCart(userId);
        List<CartItem> items = cartService.listItems(userId);
        return CartResponse.from(cart.getId(), cart.getUser().getId(), items);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItem(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @Valid @RequestBody AddCartItemRequest request) {
        CartItem item = cartService.addItem(userId, request.productId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(CartItemResponse.from(item));
    }

    @PutMapping("/items/{productId}")
    public CartItemResponse updateItem(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Product id must be positive") Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartItem item = cartService.updateItemQuantity(userId, productId, request.quantity());
        return CartItemResponse.from(item);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(
            @RequestHeader("X-User-Id") @Positive(message = "User id must be positive") Long userId,
            @PathVariable @Positive(message = "Product id must be positive") Long productId) {
        cartService.removeItem(userId, productId);
        return ResponseEntity.noContent().build();
    }
}
