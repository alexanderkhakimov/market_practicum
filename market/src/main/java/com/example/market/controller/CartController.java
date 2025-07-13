package com.example.market.controller;

import com.example.market.helper.CartAction;
import com.example.market.model.Order;
import com.example.market.model.OrderItem;
import com.example.market.service.CartService;
import com.example.market.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
public class CartController {
    private final OrderService orderService;

    private final CartService cartService;

    public CartController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }


    @GetMapping("/cart/items")
    public Mono<String> getCartPage(Model model) {
        return orderService.findOrderByStatus("CART")
                .flatMap(cart -> {
                    List<OrderItem> items = cart.getOrderItems() != null ?
                            cart.getOrderItems() : Collections.emptyList();

                    // Обертываем синхронный вызов в Mono
                    BigDecimal total = items.isEmpty() ? BigDecimal.ZERO : cart.totalSum();

                    model.addAttribute("items", items);
                    model.addAttribute("total", total);
                    model.addAttribute("empty", items.isEmpty());

                    return Mono.just("cart");
                })
                .onErrorResume(e -> {
                    model.addAttribute("error", "Ошибка загрузки корзины: " + e.getMessage());
                    model.addAttribute("items", Collections.emptyList());
                    model.addAttribute("total", BigDecimal.ZERO);
                    model.addAttribute("empty", true);
                    return Mono.just("cart");
                });
    }

    @PostMapping("/cart/items/{id}")
    public Mono<String> updateCart(
            @PathVariable Long id,
            @RequestParam("action") String action) {
        return cartService.updateCart(id, CartAction.valueOf(action.toUpperCase()))
                .thenReturn("redirect:/cart/items");
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return orderService.findOrderByStatus("CART")
                .filter(cart -> !cart.getOrderItems().isEmpty())
                .flatMap(cart -> {
                    cart.setStatus("PENDING");
                    return orderService.saveOrder(cart)
                            .thenReturn ("redirect:/orders/" + cart.getId() + "?newOrder=true)");
                })
                .defaultIfEmpty("redirect:/cart/items");
    }
}
