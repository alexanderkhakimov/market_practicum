package com.example.market.controller;

import com.example.market.model.Order;
import com.example.market.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public Mono<String> getOrdersPage(Model model) {
        return orderService.findAllOrderByStatusNotWithItems("CART")
                .collectList()
                .flatMap(orders -> {
                    model.addAttribute("orders", orders);
                    return Mono.just("orders");
                })
                .onErrorResume(e -> {
                    model.addAttribute("error", "Ошибка загрузки заказов");
                    return Mono.just("orders");
                });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrderPage(@PathVariable Long id,
                                     @RequestParam(defaultValue = "false") boolean newOrder,
                                     Model model) {
        return orderService.findOrderWithItems(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Заказ не найден")))
                .doOnSuccess(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }
}