package com.example.market.controller;

import com.example.market.helper.CartAction;
import com.example.market.model.Item;
import com.example.market.model.Order;
import com.example.market.repository.ItemRepository;
import com.example.market.repository.OrderRepository;
import com.example.market.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CartController {
    @Autowired
    private final ItemRepository itemRepository;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final CartService cartService;

    public CartController(ItemRepository itemRepository, OrderRepository orderRepository, CartService cartService) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    @GetMapping("/cart/items")
    public String getCartPage(Model model) {
        Order cart = orderRepository.findByStatus("CART")
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
        return orderRepository.findByStatus("CART")
                .filter(cart -> !cart.getOrderItems().isEmpty())
                .map(cart -> {
                    cart.setStatus("PENDING");
                    orderRepository.save(cart);
                    return "redirect:/orders/" + cart.getId() + "?newOrder=true";
                })
                .orElse("redirect:/cart/items");
    }
}
