package com.example.market.service;

import com.example.market.model.Order;
import com.example.market.repository.OrderItemRepository;
import com.example.market.repository.OrderRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import com.example.market.repository.ItemRepository;


@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
    }

    public Flux<Order> findAllOrderByStatusNotWithItems(String status) {
        return orderRepository.findAllByStatusNot(status)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .flatMap(orderItem -> itemRepository.findById(orderItem.getItemId())
                                .map(item -> {
                                    orderItem.setItem(item);
                                    return orderItem;
                                }))
                        .collectList()
                        .map(orderItems -> {
                            order.setOrderItems(orderItems);
                            return order;
                        }));
    }

    public Mono<Order> findOrderWithItems(Long orderId) {
        return orderRepository.findById(orderId)
                .flatMap(order -> orderItemRepository.findByOrderId(orderId)
                        .flatMap(orderItem -> itemRepository.findById(orderItem.getItemId())
                                .map(item -> {
                                    orderItem.setItem(item);
                                    return orderItem;
                                }))
                        .collectList()
                        .map(orderItems -> {
                            order.setOrderItems(orderItems);
                            return order;
                        }));
    }

    public Flux<Order> findAllOrderByStatusNot(String status) {
        return orderRepository.findAllByStatusNot(status);
    }

    public Mono<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Mono<Void> deleteOrder(Long orderId) {
        return orderItemRepository.deleteByOrderId(orderId)
                .then(orderRepository.deleteById(orderId));
    }

    public Mono<Order> saveOrder(Order order) {
        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    order.getOrderItems().forEach(item -> item.setOrderId(savedOrder.getId()));
                    return orderItemRepository.saveAll(order.getOrderItems())
                            .collectList()
                            .map(items -> {
                                order.setOrderItems(items);
                                return order;
                            });
                });
    }

    public Mono<Order> findOrderByStatus(String status) {
        return orderRepository.findByStatus(status)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .flatMap(orderItem -> itemRepository.findById(orderItem.getItemId())
                                .map(item -> {
                                    orderItem.setItem(item);
                                    return orderItem;
                                }))
                        .collectList()
                        .map(orderItems -> {
                            order.setOrderItems(orderItems);
                            return order;
                        }))
                .switchIfEmpty(createNewCart());
    }

    private Mono<Order> createNewCart() {
        Order newOrder = new Order();
        newOrder.setStatus("CART");
        newOrder.setOrderItems(new ArrayList<>());
        return orderRepository.save(newOrder);
    }
}