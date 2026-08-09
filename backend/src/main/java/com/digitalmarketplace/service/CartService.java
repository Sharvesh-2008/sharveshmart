package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Cart;
import com.digitalmarketplace.entity.CartItem;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.repository.CartItemRepository;
import com.digitalmarketplace.repository.CartRepository;
import com.digitalmarketplace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException("User not found"));
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }

    @Transactional(readOnly = true)
    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Cart not found"));
    }

    @Transactional
    public CartItem addItem(Long userId, Long productId, int quantity) {
        Product product = productService.getApprovedProduct(productId);
        Cart cart = getOrCreateCart(userId);

        return cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + quantity);
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> {
                    CartItem item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(product);
                    item.setQuantity(quantity);
                    return cartItemRepository.save(item);
                });
    }

    @Transactional
    public CartItem updateItemQuantity(Long userId, Long productId, int quantity) {
        if (quantity < 1) {
            throw new BusinessException("Quantity must be at least 1");
        }
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new BusinessException("Cart item not found"));
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new BusinessException("Cart item not found"));
        cartItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<CartItem> listItems(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("Cart not found"));
        return cartItemRepository.findByCartId(cart.getId());
    }
}