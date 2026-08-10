package com.digitalmarketplace.controller;

import com.digitalmarketplace.dto.OrderDetailResponse;
import com.digitalmarketplace.dto.OrderSummaryResponse;
import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderItem;
import com.digitalmarketplace.security.UserPrincipal;
import com.digitalmarketplace.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Validated
@Tag(name = "Orders", description = "Order checkout and history")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Checkout the current user's cart into an order")
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDetailResponse> checkout(@AuthenticationPrincipal UserPrincipal principal) {
        Order order = orderService.createOrderFromCart(principal.getId());
        List<OrderItem> items = orderService.listOrderItems(order.getId());
        OrderDetailResponse response = OrderDetailResponse.from(order, items);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<OrderSummaryResponse> listOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return orderService.listOrdersByUser(principal.getId()).stream().map(OrderSummaryResponse::from).toList();
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public OrderDetailResponse getOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "Order id must be positive") Long orderId) {
        Order order = orderService.getOrder(orderId);
        List<OrderItem> items = orderService.listOrderItems(orderId);
        return OrderDetailResponse.from(order, items);
    }
}
