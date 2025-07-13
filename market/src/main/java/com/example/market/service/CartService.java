package com.example.market.service;

import com.example.market.helper.CartAction;
import com.example.market.model.Item;
import com.example.market.model.Order;
import com.example.market.model.OrderItem;
import com.example.market.repository.ItemRepository;
import com.example.market.repository.OrderItemRepository;
import com.example.market.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public CartService(ItemRepository itemRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.itemRepository = itemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        logger.info("CartService initialized");
    }

    public Mono<Map<Long, Integer>> getCart() {
        logger.debug("Getting cart contents");
        return getOrCreateCart()
                .flatMapIterable(Order::getOrderItems)
                .filter(orderItem -> orderItem.getItem() != null)
                .collectMap(
                        orderItem -> orderItem.getItem().getId(),
                        OrderItem::getCount
                )
                .onErrorReturn(Collections.emptyMap())
                .doOnSuccess(map -> logger.debug("Retrieved cart with {} items", map.size()))
                .doOnError(e -> logger.error("Error getting cart", e));
    }

    private Mono<Order> getOrCreateCart() {
        logger.debug("Getting or creating cart");
        return orderRepository.findByStatus("CART")
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .map(orderItems -> {
                            order.setOrderItems(orderItems);
                            logger.debug("Found existing cart with id: {}", order.getId());
                            return order;
                        }))
                .switchIfEmpty(Mono.defer(() -> {
                    logger.debug("No existing cart found, creating new one");
                    return createNewCart();
                }))
                .onErrorResume(e -> {
                    logger.error("Error in getOrCreateCart", e);
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Ошибка сервера",
                            e
                    ));
                });
    }

    private Mono<Order> createNewCart() {
        return Mono.just(new Order())
                .doOnNext(cart -> {
                    cart.setStatus("CART");
                    logger.debug("Creating new cart");
                })
                .flatMap(orderRepository::save)
                .doOnSuccess(order -> logger.info("Created new cart with id: {}", order.getId()));
    }

    @Transactional
    public Mono<Void> updateCart(Long itemId, CartAction action) {
        logger.info("Updating cart for itemId: {} with action: {}", itemId, action);
        return Mono.zip(
                        getOrCreateCart(),
                        itemRepository.findById(itemId)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Item not found")))
                )
                .flatMap(tuple -> {
                    Order cart = tuple.getT1();
                    Item item = tuple.getT2();
                    logger.debug("Processing cart update for item: {}", item.getTitle());

                    return findOrderItemInCart(cart, itemId)
                            .flatMap(orderItem -> {
                                logger.debug("Found existing order item for product: {}", item.getTitle());
                                return handleExistingItem(cart, orderItem, action);
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                logger.debug("No existing order item found, creating new one");
                                return handleNewItem(cart, item, action);
                            }));
                })
                .then()
                .doOnSuccess(v -> logger.info("Cart updated successfully"))
                .doOnError(e -> logger.error("Error updating cart", e));
    }

    private Mono<OrderItem> findOrderItemInCart(Order cart, Long itemId) {
        logger.debug("Looking for order item with itemId: {} in cart", itemId);
        return Flux.fromIterable(cart.getOrderItems())
                .filter(oi -> oi.getItem().getId().equals(itemId))
                .singleOrEmpty()
                .doOnSuccess(oi -> {
                    if (oi != null) {
                        logger.debug("Found order item for itemId: {}", itemId);
                    } else {
                        logger.debug("No order item found for itemId: {}", itemId);
                    }
                });
    }

    private Mono<Order> handleExistingItem(Order cart, OrderItem orderItem, CartAction action) {
        logger.debug("Handling existing item with action: {}", action);
        switch (action) {
            case PLUS:
                orderItem.setCount(orderItem.getCount() + 1);
                logger.debug("Increased quantity for item: {}", orderItem.getItem().getTitle());
                break;
            case MINUS:
                if (orderItem.getCount() > 1) {
                    orderItem.setCount(orderItem.getCount() - 1);
                    logger.debug("Decreased quantity for item: {}", orderItem.getItem().getTitle());
                } else {
                    cart.getOrderItems().remove(orderItem);
                    logger.debug("Removed item from cart: {}", orderItem.getItem().getTitle());
                }
                break;
            case DELETE:
                cart.getOrderItems().remove(orderItem);
                logger.debug("Deleted item from cart: {}", orderItem.getItem().getTitle());
                break;
        }
        return orderRepository.save(cart)
                .doOnSuccess(o -> logger.debug("Cart saved after item update"));
    }

    private Mono<Order> handleNewItem(Order cart, Item item, CartAction action) {
        if (action != CartAction.PLUS) {
            logger.debug("Action {} not applicable for new item, skipping", action);
            return Mono.just(cart);
        }

        logger.debug("Adding new item to cart: {}", item.getTitle());
        return Mono.just(new OrderItem())
                .doOnNext(newOrderItem -> {
                    newOrderItem.setOrderId(cart.getId());
                    newOrderItem.setItem(item);
                    newOrderItem.setCount(1);
                    newOrderItem.setPriceAtOrder(item.getPrice());
                    logger.debug("Created new order item for product: {}", item.getTitle());
                })
                .doOnNext(cart.getOrderItems()::add)
                .thenReturn(cart)
                .flatMap(orderRepository::save)
                .doOnSuccess(o -> logger.info("Added new item to cart: {}", item.getTitle()));
    }
}