package com.example.market.controller;

import com.example.market.helper.CartAction;
import com.example.market.helper.SortType;
import com.example.market.model.Item;
import com.example.market.repository.ItemRepository;
import com.example.market.service.CartService;
import com.example.market.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/main")
public class ItemController {

    private final ItemService itemService;

    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String redirectToItems() {
        return "redirect:/main/items";
    }

    @GetMapping("/items")
    public String getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber,
            Model model) {
        PageRequest pageRequest;
        switch (sort) {
            case ALPHA:
                pageRequest = PageRequest.of(pageNumber - 1, pageSize, Sort.by("title"));
                break;
            case PRICE:
                pageRequest = PageRequest.of(pageNumber - 1, pageSize, Sort.by("price"));
                break;
            default:
                pageRequest = PageRequest.of(pageNumber - 1, pageSize);
        }

        Page<Item> itemPage = itemService.findBySearch(search, pageRequest);

        int itemsPerRow = 3;
        List<List<Item>> items = new ArrayList<>();
        List<Item> currentRow = new ArrayList<>();

        for (Item item : itemPage.getContent()) {
            currentRow.add(item);
            if (currentRow.size() == itemsPerRow) {
                items.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }
        if (!currentRow.isEmpty()) {
            items.add(currentRow);
        }

        model.addAttribute("items", items);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", Map.of(
                "pageNumber", pageNumber,
                "pageSize", pageSize,
                "hasNext", itemPage.hasNext(),
                "hasPrevious", itemPage.hasPrevious()
        ));
        model.addAttribute("cart", cartService.getCart());

        return "main";
    }

    @GetMapping("/items/{id}")
    public String getItemPage(@PathVariable Long id, Model model) {
        Item item = itemService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найдет!"));
        model.addAttribute("item", item);
        return "item";
    }

    @PostMapping("/items/{id}")
    public String updateCart(
            @PathVariable Long id,
            @RequestParam("action") String action) {
        cartService.updateCart(id, CartAction.valueOf(action.toUpperCase()));
        return "redirect:/main/items";
    }
}



