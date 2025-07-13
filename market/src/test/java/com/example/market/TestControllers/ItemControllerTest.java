package com.example.market.TestControllers;

import com.example.market.controller.ItemController;
import com.example.market.model.Item;
import com.example.market.service.CartService;
import com.example.market.service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.when;
@WebFluxTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CartService cartService;


    @Test
    void testGetItemPage_found() {

        Item item = new Item();
        item.setId(1L);
        item.setTitle("Test Item");

        Mockito.when(itemService.findById(1L))
                .thenReturn(Mono.just(item));

        // Act & Assert
        webTestClient.get()
                .uri("/main/items/1")
                .accept(MediaType.TEXT_HTML) // Ожидаем HTML ответ
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> {
                    String responseBody = new String(result.getResponseBody());
                    assertTrue(responseBody.contains("Test Item"));
                });
    }

    @Test
    void testGetItemPageFound() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Test Item");
        item.setPrice(new BigDecimal("99.99"));
        item.setDescription("Description");
        item.setImgPath("image.jpg");

        when(itemService.findById(1L))
                .thenReturn(Mono.just(item));
        when(cartService.getCart())
                .thenReturn(Mono.just(Map.of()));

        webTestClient.get()
                .uri("/main/items/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String html = result.getResponseBody();
                    assertNotNull(html);
                    assertTrue(html.contains("Test Item"));
                    assertTrue(html.contains("99.99"));
                });
    }
}