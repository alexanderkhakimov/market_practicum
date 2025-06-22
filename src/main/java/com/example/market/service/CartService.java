package com.example.market.service;

import com.example.market.helper.CartAction;
import com.example.market.model.Item;
import com.example.market.model.Order;
import com.example.market.model.OrderItem;
import com.example.market.repository.ItemRepository;
import com.example.market.repository.OrderRepository;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CartService {
    private final ItemRepository itemRepository;

    private final OrderRepository orderRepository;

    public CartService(ItemRepository itemRepository, OrderRepository orderRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
    }

    public Mono<Map<Long, Integer>> getCart() {

        return getOrCreateCart()
                .flatMapIterable(Order::getOrderItems)
                .filter(orderItem -> orderItem.getItem()!=null)
                .collectMap(
                        orderItem -> orderItem.getItem().getId(),
                        OrderItem::getCount
                )
                .onErrorReturn(Collections.emptyMap());
    }
    private Mono<Order> getOrCreateCart() {
        return orderRepository.findByStatus("CART")
                .switchIfEmpty(Mono.defer(this::createNewCart))
                .onErrorResume(e -> {
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Ошибка сервера",
                            e
                    ));
                });
    }

    private Mono<Order> createNewCart() {
        return Mono.just(new Order())
                .doOnNext(cart -> cart.setStatus("CART"))
                .flatMap(orderRepository::save);
    }

    @Transactional
    public Mono<Void> updateCart(Long itemId, CartAction action) {
        return Mono.zip(
                        getOrCreateCart(),
                        itemRepository.findById(itemId)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")))
                )
                .flatMap(tuple -> {
                    Order cart = tuple.getT1();
                    Item item = tuple.getT2();

                    return findOrderItemInCart(cart, itemId)
                            .flatMap(orderItem -> handleExistingItem(cart, orderItem, action))
                            .switchIfEmpty(Mono.defer(() -> handleNewItem(cart, item, action)));
                })
                .then();
    }

    private Mono<OrderItem> findOrderItemInCart(Order cart, Long itemId) {
        return Flux.fromIterable(cart.getOrderItems())
                .filter(oi -> oi.getItem().getId().equals(itemId))
                .next();
    }

    private Mono<Order> handleExistingItem(Order cart, OrderItem orderItem, CartAction action) {
        switch (action) {
            case PLUS:
                orderItem.setCount(orderItem.getCount() + 1);
                break;
            case MINUS:
                if (orderItem.getCount() > 1) {
                    orderItem.setCount(orderItem.getCount() - 1);
                } else {
                    cart.getOrderItems().remove(orderItem);
                }
                break;
            case DELETE:
                cart.getOrderItems().remove(orderItem);
                break;
        }
        return orderRepository.save(cart);
    }

    private Mono<Order> handleNewItem(Order cart, Item item, CartAction action) {
        if (action != CartAction.PLUS) {
            return Mono.just(cart);
        }

        return Mono.just(new OrderItem())
                .doOnNext(newOrderItem->{
                    newOrderItem.setOrder(cart);
                newOrderItem.setItem(item);
                newOrderItem.setCount(1);
                newOrderItem.setPriceAtOrder(item.getPrice());
                })
                .doOnNext(cart.getOrderItems()::add)
                .thenReturn(cart)
                .flatMap(orderRepository::save);
    }

}
