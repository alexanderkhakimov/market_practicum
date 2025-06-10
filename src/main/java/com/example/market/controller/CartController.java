package com.example.market.controller;

import com.example.market.helper.CartAction;
import com.example.market.model.Order;
import com.example.market.service.CartService;
import com.example.market.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;

@Controller
public class CartController {
    private final OrderService orderService;

    private final CartService cartService;

    public CartController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/cart/items")
    public String getCartPage(Model model) {
        Order cart = orderService.findOrderByStatus("CART")
                .orElseThrow(() -> new IllegalArgumentException("Корзина пустая!"));
        BigDecimal total = BigDecimal.ZERO;
        if (cart != null) {
            total = cart.totalSum();
        }
        model.addAttribute("items", cart != null ? cart.getOrderItems() : new ArrayList<>());
        model.addAttribute("total", total);
        model.addAttribute("empty", cart == null || cart.getOrderItems().isEmpty());

        return "cart";
    }

    @PostMapping("/cart/items/{id}")
    public String updateCart(
            @PathVariable Long id,
            @RequestParam("action") String action) {
        cartService.updateCart(id, CartAction.valueOf(action.toUpperCase()));
        return "redirect:/cart/items";
    }

    @PostMapping("/buy")
    public String buy() {
        return orderService.findOrderByStatus("CART")
                .filter(cart -> !cart.getOrderItems().isEmpty())
                .map(cart -> {
                    cart.setStatus("PENDING");
                    orderService.saveOrder(cart);
                    return "redirect:/orders/" + cart.getId() + "?newOrder=true";
                })
                .orElse("redirect:/cart/items");
    }
}
