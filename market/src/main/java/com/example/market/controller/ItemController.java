package com.example.market.controller;

import com.example.market.enums.CartAction;
import com.example.market.enums.SortType;
import com.example.market.model.Item;
import com.example.market.service.CartService;
import com.example.market.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/main")
public class ItemController {
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public Mono<String> redirectToItems() {
        return Mono.just("redirect:/main/items");
    }

    @GetMapping("/items")
    public Mono<String> getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber,
            Model model,
            Authentication authentication) {

        PageRequest pageRequest = PageRequest.of(
                pageNumber - 1, // Spring Data использует нумерацию с 0
                pageSize,
                sort == SortType.ALPHA ? Sort.by("title") :
                        sort == SortType.PRICE ? Sort.by("price") :
                                Sort.unsorted()
        );

        return Mono.zip(
                itemService.findBySearch(search, pageRequest).collectList(),
                itemService.countBySearch(search)
        ).flatMap(tuple -> {
            List<Item> items = tuple.getT1();
            long totalCount = tuple.getT2();

            // Группируем товары по 3 в строку
            List<List<Item>> groupedItems = new ArrayList<>();
            for (int i = 0; i < items.size(); i += 3) {
                groupedItems.add(items.subList(i, Math.min(i + 3, items.size())));
            }

            // Рассчитываем пагинацию
            boolean hasNext = (pageNumber * pageSize) < totalCount;
            boolean hasPrevious = pageNumber > 1;

            // Заполняем модель
            model.addAttribute("items", groupedItems);
            model.addAttribute("search", search);
            model.addAttribute("sort", sort);
            model.addAttribute("paging", Map.of(
                    "pageSize", pageSize,
                    "pageNumber", pageNumber,
                    "hasNext", hasNext,
                    "hasPrevious", hasPrevious
            ));
            model.addAttribute("isAuthenticated", authentication != null && authentication.isAuthenticated());

            return cartService.getCart()
                    .doOnSuccess(cart -> model.addAttribute("cart", cart))
                    .thenReturn("main");
        }).onErrorResume(e -> {
            model.addAttribute("paging", Map.of(
                    "pageSize", pageSize,
                    "pageNumber", pageNumber,
                    "hasNext", false,
                    "hasPrevious", false
            ));
            model.addAttribute("error", "Ошибка загрузки товаров");
            model.addAttribute("items", Collections.emptyList());
            model.addAttribute("isAuthenticated", authentication != null && authentication.isAuthenticated());
            return Mono.just("main");
        });
    }

    @GetMapping("/items/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getItemPage(@PathVariable Long id, Model model) {
        return itemService.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Товар не найден!")))
                .flatMap(item -> {
                    model.addAttribute("item", item);
                    return cartService.getCart()
                            .doOnSuccess(cart -> model.addAttribute("cart", cart))
                            .thenReturn("item");
                });
    }

    @PostMapping("/items/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> updateCart(
            @PathVariable Long id,
            @RequestParam(value = "action", required = false) String action) {
        logger.info("Received updateCart request for itemId: {} with action: {}", id, action);
        if (action == null || action.trim().isEmpty()) {
            logger.error("Action parameter is missing or empty for itemId: {}", id);
            return Mono.just("redirect:/main/items?error=" + URLEncoder.encode("Параметр action отсутствует", StandardCharsets.UTF_8));
        }
        try {
            CartAction cartAction = CartAction.valueOf(action.toUpperCase());
            return cartService.updateCart(id, cartAction)
                    .thenReturn("redirect:/main/items")
                    .onErrorResume(e -> {
                        logger.error("Error updating cart for itemId: {}", id, e);
                        return Mono.just("redirect:/main/items?error=" + URLEncoder.encode("Ошибка при обновлении корзины: " + e.getMessage(), StandardCharsets.UTF_8));
                    });
        } catch (IllegalArgumentException e) {
            logger.error("Invalid action value: {} for itemId: {}", action, id, e);
            return Mono.just("redirect:/main/items?error=" + URLEncoder.encode("Недопустимое действие: " + action, StandardCharsets.UTF_8));
        }
    }
}