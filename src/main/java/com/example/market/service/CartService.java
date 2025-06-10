package com.example.market.service;

import com.example.market.helper.CartAction;
import com.example.market.model.Item;
import com.example.market.model.Order;
import com.example.market.model.OrderItem;
import com.example.market.repository.ItemRepository;
import com.example.market.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final ItemRepository itemRepository;

    private final OrderRepository orderRepository;

    public CartService(ItemRepository itemRepository, OrderRepository orderRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    private Order getOrCreateCart() {
        Optional<Order> cart = orderRepository.findByStatus("CART");
        if (cart.isPresent()) {
            return cart.get();
        } else {
            Order newCart = new Order();
            newCart.setStatus("CART");
            return orderRepository.save(newCart);
        }
    }

    @Transactional
    public void updateCart(Long itemId, CartAction action) {
        Order cart = getOrCreateCart();
        Optional<Item> itemOptional = itemRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            return; // Товар не найден
        }
        Item item = itemOptional.get();

        // Ищем существующий OrderItem
        Optional<OrderItem> existingOrderItem = cart.getOrderItems().stream()
                .filter(oi -> oi.getItem().getId().equals(itemId))
                .findFirst();

        switch (action) {
            case PLUS:
                if (existingOrderItem.isPresent()) {
                    OrderItem orderItem = existingOrderItem.get();
                    orderItem.setCount(orderItem.getCount() + 1);
                } else {
                    OrderItem newOrderItem = new OrderItem();
                    newOrderItem.setOrder(cart);
                    newOrderItem.setItem(item);
                    newOrderItem.setCount(1);
                    newOrderItem.setPriceAtOrder(item.getPrice());
                    cart.getOrderItems().add(newOrderItem);
                }
                break;
            case MINUS:
                if (existingOrderItem.isPresent()) {
                    OrderItem orderItem = existingOrderItem.get();
                    if (orderItem.getCount() > 1) {
                        orderItem.setCount(orderItem.getCount() - 1);
                    } else {
                        cart.getOrderItems().remove(orderItem);
                    }
                }
                break;
            case DELETE:
                if (existingOrderItem.isPresent()) {
                    cart.getOrderItems().remove(existingOrderItem.get());
                }
                break;
        }
        orderRepository.save(cart);
    }

    public Map<Long, Integer> getCart() {
        Order cart = getOrCreateCart();
        Map<Long, Integer> cartItems = new HashMap<>();
        for (OrderItem orderItem : cart.getOrderItems()) {
            cartItems.put(orderItem.getItem().getId(), orderItem.getCount());
        }
        return cartItems;
    }
}
