package com.example.market;

import com.example.market.controller.OrderController;
import com.example.market.model.Item;
import com.example.market.model.Order;
import com.example.market.model.OrderItem;
import com.example.market.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void testGetOrdersPage() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("PENDING");
        OrderItem item = new OrderItem();
        Item product = new Item();
        product.setPrice(new BigDecimal("50.00"));
        item.setItem(product);
        item.setPriceAtOrder(new BigDecimal("50.00"));
        item.setCount(1);
        order.setOrderItems(List.of(item));
        List<Order> orders = new ArrayList<>(List.of(order)); // Преобразуем в ArrayList
        when(orderService.findAllOrderByStatusNot("CART")).thenReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", orders));

        verify(orderService, times(1)).findAllOrderByStatusNot("CART");
    }

    @Test
    void testGetOrderPage_orderFound_newOrderTrue() throws Exception {

        Order order = new Order();
        order.setId(1L);
        order.setStatus("PENDING");
        OrderItem item = new OrderItem();
        Item product = new Item();
        product.setPrice(new BigDecimal("50.00"));
        item.setItem(product);
        item.setPriceAtOrder(new BigDecimal("50.00"));
        item.setCount(1);
        order.setOrderItems(List.of(item));
        when(orderService.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        mockMvc.perform(get("/orders/1").param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", order))
                .andExpect(model().attribute("newOrder", true));

        verify(orderService, times(1)).findById(1L);
    }

    @Test
    void testGetOrderPage_orderFound_newOrderFalse() throws Exception {
        Order order = new Order();
        order.setId(1L);
        order.setStatus("PENDING");
        OrderItem item = new OrderItem();
        Item product = new Item();
        product.setPrice(new BigDecimal("50.00"));
        item.setItem(product);
        item.setPriceAtOrder(new BigDecimal("50.00"));
        item.setCount(1);
        order.setOrderItems(List.of(item));
        when(orderService.findById(1L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", order))
                .andExpect(model().attribute("newOrder", false));

        verify(orderService, times(1)).findById(1L);
    }

}