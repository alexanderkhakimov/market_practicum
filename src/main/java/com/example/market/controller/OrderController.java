package com.example.market.controller;

import com.example.market.model.Order;
import com.example.market.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String getOrdersPage(Model model) {
        model.addAttribute("orders", orderService.findAllOrderByStatusNot("CART"));
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrderPage(@PathVariable Long id,
                               @RequestParam(defaultValue = "false") boolean newOrder,
                               Model model) {

        Order order = orderService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);

        return "order";
    }

}
