package com.example.market.controller;

import com.example.market.model.Item;
import com.example.market.model.Order;
import com.example.market.repository.ItemRepository;
import com.example.market.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {
    @Autowired
    private final ItemRepository itemRepository;
    @Autowired
    private final OrderRepository orderRepository;

    public CartController(ItemRepository itemRepository, OrderRepository orderRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/cart/items")
    public String getCartPage(Model model) {
      Order cart = orderRepository.findByStatus("CART")
              .orElseThrow(()->new IllegalArgumentException("Корзина пустая!"));
        BigDecimal total = BigDecimal.ZERO;
        if(cart != null){
            total=cart.totalSum();
        }
        model.addAttribute("items", cart != null ? cart.getOrderItems() : new ArrayList<>());
        model.addAttribute("total", total);
        model.addAttribute("empty", cart == null || cart.getOrderItems().isEmpty());

        return "cart";
    }
}
