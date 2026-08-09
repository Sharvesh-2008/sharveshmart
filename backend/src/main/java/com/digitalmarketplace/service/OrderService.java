package com.digitalmarketplace.service;

import com.digitalmarketplace.entity.Cart;
import com.digitalmarketplace.entity.CartItem;
import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderItem;
import com.digitalmarketplace.entity.OrderStatus;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.User;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.exception.ResourceNotFoundException;
import com.digitalmarketplace.repository.CartRepository;
import com.digitalmarketplace.repository.OrderItemRepository;
import com.digitalmarketplace.repository.OrderRepository;
import com.digitalmarketplace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartRepository cartRepository,
                        UserRepository userRepository,
                        ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @Transactional
    public Order createOrderFromCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        List<CartItem> items = cart.getItems();
        if (items == null || items.isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : items) {
            Product product = productService.getApprovedProduct(cartItem.getProduct().getId());
            BigDecimal unitPrice = product.getPrice();
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItems.add(orderItem);
        }

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Order total must be greater than zero");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);
        order.setItems(orderItems);
        orderItems.forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);
        return saved;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Transactional(readOnly = true)
    public List<Order> listOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> listOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}