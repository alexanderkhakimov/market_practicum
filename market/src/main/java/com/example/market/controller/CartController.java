package com.example.market.controller;

import com.example.market.enums.CartAction;
import com.example.market.model.OrderItem;
import com.example.market.service.CartService;
import com.example.market.service.OrderService;
import com.example.market.client.PaymentClientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentClientService paymentClientService;

    public CartController(OrderService orderService, CartService cartService, PaymentClientService paymentClientService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.paymentClientService = paymentClientService;
    }

    @GetMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getCartPage(Model model) {
        return orderService.findOrderByStatus("CART")
                .flatMap(cart -> {
                    List<OrderItem> items = cart.getOrderItems() != null ?
                            cart.getOrderItems() : Collections.emptyList();
                    BigDecimal total = items.isEmpty() ? BigDecimal.ZERO : cart.totalSum();

                    return paymentClientService.getBalance()
                            .map(balanceResponse -> {
                                String balance = balanceResponse.getBalance();
                                model.addAttribute("items", items);
                                model.addAttribute("total", total);
                                model.addAttribute("empty", items.isEmpty());
                                model.addAttribute("balance", balance);
                                model.addAttribute("canPay", new BigDecimal(balance).compareTo(total) >= 0);
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

    @PostMapping("/items/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> updateCart(
            @PathVariable Long id,
            @RequestParam("action") String action) {
        return cartService.updateCart(id, CartAction.valueOf(action.toUpperCase()))
                .thenReturn("redirect:/cart/items")
                .onErrorResume(e -> Mono.just("redirect:/cart/items?error=" +
                        java.net.URLEncoder.encode("Ошибка обновления корзины: " + e.getMessage(),
                                java.nio.charset.StandardCharsets.UTF_8)));
    }

    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> buy(@RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
        return orderService.findOrderByStatus("CART")
                .filter(cart -> !cart.getOrderItems().isEmpty())
                .flatMap(cart -> {
                    BigDecimal amount = cart.totalSum();
                    String orderId = String.valueOf(cart.getId());
                    String paymentMethod = "CARD";

                    return paymentClientService.processPayment(authorizedClient, orderId, amount, paymentMethod)
                            .flatMap(paymentResponse -> {
                                if ("SUCCESS".equals(paymentResponse.getStatus())) {
                                    cart.setStatus("PAID");
                                    return orderService.saveOrder(cart)
                                            .thenReturn("redirect:/orders/" + cart.getId() + "?newOrder=true");
                                } else {
                                    return Mono.just("redirect:/cart/items?error=" +
                                            java.net.URLEncoder.encode("Payment failed: " + paymentResponse.getStatus(),
                                                    java.nio.charset.StandardCharsets.UTF_8));
                                }
                            })
                            .onErrorResume(e -> {
                                String errorMessage = e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка при оплате";
                                return Mono.just("redirect:/cart/items?error=" +
                                        java.net.URLEncoder.encode(errorMessage,
                                                java.nio.charset.StandardCharsets.UTF_8));
                            });
                })
                .defaultIfEmpty("redirect:/cart/items?error=" +
                        java.net.URLEncoder.encode("Cart is empty",
                                java.nio.charset.StandardCharsets.UTF_8));
    }
}