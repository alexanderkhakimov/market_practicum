package com.example.market.TestIntegrations;

import com.example.market.model.Item;
import com.example.market.model.Order;
import com.example.market.model.OrderItem;
import com.example.market.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    void testGetOrdersPage_noOrders() {
        // Настраиваем mock для пустого списка заказов
        when(orderService.findAllOrderByStatusNotWithItems("CART"))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String response = result.getResponseBody();
                    assertNotNull(response);
                    assertTrue(response.contains("Нет заказов"));
                    assertFalse(response.contains("Заказ №"));
                });
    }

    @Test
    void testGetOrdersPage_withOrders() {
        // Создаем тестовые данные
        Item item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Телефон");
        item1.setPrice(new BigDecimal("500.00")); // Цена за единицу

        Item item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Ноутбук");
        item2.setPrice(new BigDecimal("1500.00")); // Цена за единицу

        OrderItem orderItem1 = new OrderItem();
        orderItem1.setItem(item1);
        orderItem1.setCount(2);
        orderItem1.setPriceAtOrder(item1.getPrice().multiply(new BigDecimal(orderItem1.getCount()))); // 500 * 2 = 1000

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setItem(item2);
        orderItem2.setCount(1);
        orderItem2.setPriceAtOrder(item2.getPrice()); // 1500 * 1 = 1500

        Order order = new Order();
        order.setId(1L);
        order.setStatus("COMPLETED");
        order.setOrderItems(List.of(orderItem1, orderItem2));


        when(orderService.findAllOrderByStatusNotWithItems("CART"))
                .thenReturn(Flux.just(order));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    String response = result.getResponseBody();
                    assertNotNull(response);

                    assertTrue(response.contains("Заказы"));
                    assertTrue(response.contains("Заказ №1"));

                    assertTrue(response.contains("Телефон (2 шт.) 2000.00 руб."));
                    assertTrue(response.contains("Ноутбук (1 шт.) 1500.00 руб."));

                    assertTrue(response.contains("Сумма: 3500.00 руб."));
                });
    }
}