package com.digitalmarketplace.controller;

import com.digitalmarketplace.entity.Order;
import com.digitalmarketplace.entity.OrderItem;
import com.digitalmarketplace.entity.OrderStatus;
import com.digitalmarketplace.entity.Product;
import com.digitalmarketplace.entity.UserRole;
import com.digitalmarketplace.exception.BusinessException;
import com.digitalmarketplace.exception.ResourceNotFoundException;
import com.digitalmarketplace.service.OrderService;
import com.digitalmarketplace.support.SecurityTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityTestSupport.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        SecurityTestSupport.authenticate(2L, "buyer@example.com", UserRole.USER);
    }

    @AfterEach
    void tearDown() {
        SecurityTestSupport.clear();
    }

    private Order order(long id, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setTotalAmount(new BigDecimal("39.98"));
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private OrderItem orderItem() {
        Product product = new Product();
        product.setId(3L);
        product.setTitle("Spring Guide");

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProduct(product);
        item.setUnitPrice(new BigDecimal("19.99"));
        item.setQuantity(2);
        return item;
    }

    @Test
    void checkoutReturnsCreatedWithItems() throws Exception {
        when(orderService.createOrderFromCart(2L)).thenReturn(order(7L, OrderStatus.PENDING));
        when(orderService.listOrderItems(7L)).thenReturn(List.of(orderItem()));

        mockMvc.perform(post("/api/orders/checkout"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(39.98))
                .andExpect(jsonPath("$.items[0].productTitle").value("Spring Guide"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void checkoutWhenCartEmptyReturnsBadRequest() throws Exception {
        when(orderService.createOrderFromCart(2L))
                .thenThrow(new BusinessException("Cart is empty"));

        mockMvc.perform(post("/api/orders/checkout"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listOrdersReturnsSummaries() throws Exception {
        when(orderService.listOrdersByUser(2L))
                .thenReturn(List.of(order(7L, OrderStatus.PAID), order(8L, OrderStatus.PENDING)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[1].id").value(8))
                .andExpect(jsonPath("$[1].status").value("PENDING"));
    }

    @Test
    void getOrderReturnsDetail() throws Exception {
        when(orderService.getOrder(7L)).thenReturn(order(7L, OrderStatus.PENDING));
        when(orderService.listOrderItems(7L)).thenReturn(List.of(orderItem()));

        mockMvc.perform(get("/api/orders/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.items[0].productId").value(3));
    }

    @Test
    void getOrderWhenNotFoundReturnsNotFound() throws Exception {
        when(orderService.getOrder(99L))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderWithInvalidIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/orders/0"))
                .andExpect(status().isBadRequest());
    }
}
