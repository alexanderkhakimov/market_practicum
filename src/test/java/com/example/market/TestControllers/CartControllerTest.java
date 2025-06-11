package com.example.market.TestControllers;

import com.example.market.controller.CartController;
import com.example.market.helper.CartAction;
import com.example.market.model.Item;
import com.example.market.model.Order;
import com.example.market.model.OrderItem;
import com.example.market.service.CartService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CartService cartService;

    @Test
    void testGetCartPage_cartFound() throws Exception {

        Order cart = new Order();
        cart.setId(1L);
        cart.setStatus("CART");

        OrderItem item = new OrderItem();
        Item product = new Item(); // Создаём Item, если он нужен для других целей
        product.setPrice(new BigDecimal("50.00"));
        item.setItem(product);
        item.setPriceAtOrder(new BigDecimal("50.00")); // Устанавливаем priceAtOrder
        item.setCount(1); // Устанавливаем количество
        cart.setOrderItems(List.of(item));

        when(orderService.findOrderByStatus("CART")).thenReturn(Optional.of(cart));


        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", cart.getOrderItems()))
                .andExpect(model().attribute("total", new BigDecimal("50.00")))
                .andExpect(model().attribute("empty", false));

        verify(orderService, times(1)).findOrderByStatus("CART");
    }


    @Test
    void testUpdateCart() throws Exception {

        mockMvc.perform(post("/cart/items/1")
                        .param("action", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        verify(cartService, times(1)).updateCart(1L, CartAction.DELETE);
    }

    @Test
    void testBuy_cartFoundWithItems() throws Exception {

        Order cart = new Order();
        cart.setId(1L);
        cart.setStatus("CART");

        OrderItem item = new OrderItem();
        Item product = new Item();
        product.setPrice(new BigDecimal("50.00"));
        item.setItem(product);
        item.setPriceAtOrder(new BigDecimal("50.00"));
        item.setCount(1);
        cart.setOrderItems(List.of(item));

        when(orderService.findOrderByStatus("CART")).thenReturn(Optional.of(cart));
        doNothing().when(orderService).saveOrder(cart);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1?newOrder=true"));

        verify(orderService, times(1)).findOrderByStatus("CART");
        verify(orderService, times(1)).saveOrder(cart);
        assertEquals("PENDING", cart.getStatus());
    }

    @Test
    void testBuy_cartEmpty() throws Exception {

        Order cart = new Order();
        cart.setId(1L);
        cart.setStatus("CART");
        cart.setOrderItems(new ArrayList<>());
        when(orderService.findOrderByStatus("CART")).thenReturn(Optional.of(cart));


        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        verify(orderService, times(1)).findOrderByStatus("CART");
        verify(orderService, never()).saveOrder(any());
    }

    @Test
    void testBuy_cartNotFound() throws Exception {

        when(orderService.findOrderByStatus("CART")).thenReturn(Optional.empty());


        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        verify(orderService, times(1)).findOrderByStatus("CART");

        verify(orderService, never()).saveOrder(any());
    }
}