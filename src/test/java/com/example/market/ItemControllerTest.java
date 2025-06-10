package com.example.market;

import com.example.market.controller.ItemController;
import com.example.market.helper.CartAction;
import com.example.market.helper.SortType;
import com.example.market.model.Item;
import com.example.market.service.CartService;
import com.example.market.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CartService cartService;


    @Test
    void testGetItems() throws Exception {
        // Arrange
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Test Item");
        item.setPrice(new BigDecimal("99.99"));
        Page<Item> page = new PageImpl<>(List.of(item));
        when(itemService.findBySearch(eq(""), any(PageRequest.class))).thenReturn(page);
        when(cartService.getCart()).thenReturn(new HashMap<>());

        // Act & Assert
        mockMvc.perform(get("/main/items")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageSize", "10")
                        .param("pageNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items", "search", "sort", "paging", "cart"));

        verify(itemService, times(1)).findBySearch(eq(""), any(PageRequest.class));
        verify(cartService, times(1)).getCart();
    }

    @Test
    void testGetItemPage_found() throws Exception {
        // Arrange
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Test Item");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        // Act & Assert
        mockMvc.perform(get("/main/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", item));

        verify(itemService, times(1)).findById(1L);
    }


}