package com.example.market.controller;

import com.example.market.helper.CartAction;

import com.example.market.model.OrderItem;

import com.example.market.service.CartService;
import com.example.market.service.OrderService;
import com.example.market.service.PaymentClientService;
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
    private final PaymentClientService paymentClientService;

    public CartController(OrderService orderService, CartService cartService, PaymentClientService paymentClientService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.paymentClientService = paymentClientService;
    }

    @GetMapping("/cart/items")
    public Mono<String> getCartPage(Model model) {
        return orderService.findOrderByStatus("CART")
                .flatMap(cart -> {
                    List<OrderItem> items = cart.getOrderItems() != null ?
                            cart.getOrderItems() : Collections.emptyList();
                    BigDecimal total = items.isEmpty() ? BigDecimal.ZERO : cart.totalSum();

                    // Проверяем баланс через PaymentApi
                    return paymentClientService.getBalance()
                            .map(balanceResponse -> {
                                String balance = balanceResponse.getBalance();
                                model.addAttribute("items", items);
                                model.addAttribute("total", total);
                                model.addAttribute("empty", items.isEmpty());
                                model.addAttribute("balance", balance);
                                model.addAttribute("canPay", balance.compareTo(String.valueOf(total)) >= 0);
                                return "cart";
                            });
                })
                .onErrorResume(e -> {
                    model.addAttribute("error", "Ошибка загрузки корзины: " + e.getMessage());
                    model.addAttribute("items", Collections.emptyList());
                    model.addAttribute("total", BigDecimal.ZERO);
                    model.addAttribute("empty", true);
                    model.addAttribute("balance", BigDecimal.ZERO);
                    model.addAttribute("canPay", false);
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
                    BigDecimal amount = cart.totalSum();
                    String orderId = String.valueOf(cart.getId());
                    String paymentMethod = "CARD"; // Можно сделать параметром, если нужно

                    // Вызываем PaymentApi для обработки платежа
                    return paymentClientService.processPayment(orderId, amount, paymentMethod)
                            .flatMap(paymentResponse -> {
                                if ("SUCCESS".equals(paymentResponse.getStatus())) {
                                    cart.setStatus("PAID");
                                    return orderService.saveOrder(cart)
                                            .thenReturn("redirect:/orders/" + cart.getId() + "?newOrder=true");
                                } else {
                                    return Mono.just("redirect:/cart/items?error=Payment failed: " + paymentResponse.getStatus());
                                }
                            })
                            .onErrorResume(e -> {
                                String errorMessage = e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка при оплате";
                                return Mono.just("redirect:/cart/items?error=" + errorMessage);
                            });
                })
                .defaultIfEmpty("redirect:/cart/items?error=Cart is empty");
    }
}