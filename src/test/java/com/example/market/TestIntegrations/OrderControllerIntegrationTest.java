package com.example.market.TestIntegrations;

import com.example.market.model.Item;
import com.example.market.model.Order;
import com.example.market.model.OrderItem;
import com.example.market.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

//    @Test
//    void testGetOrderPage_orderFound_newOrderTrue() throws Exception {
//
//        Order order = new Order();
//        order.setStatus("PENDING");
//
//        OrderItem item = new OrderItem();
//        Item product = new Item();
//        product.setPrice(new BigDecimal("50.00"));
//        item.setItem(product);
//        item.setPriceAtOrder(new BigDecimal("50.00"));
//        item.setCount(1);
//        order.setOrderItems(List.of(item));
//
//        doAnswer(invocation -> {
//            Order savedOrder = invocation.getArgument(0);
//            savedOrder.setId(1L);
//            return null;
//        }).when(orderService).saveOrder(any(Order.class));
//
//        orderService.saveOrder(order);
//        Long id = order.getId();
//
//        when(orderService.findById(id)).thenReturn(Optional.of(order));
//
//        mockMvc.perform(get("/orders/" + id).param("newOrder", "true"))
//                .andExpect(status().isOk())
//                .andExpect(view().name("order"))
//                .andExpect(model().attribute("order", order))
//                .andExpect(model().attribute("newOrder", true));
//    }

    @Test
    void testGetOrdersPage_noOrders() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", new ArrayList<>()));
    }

}