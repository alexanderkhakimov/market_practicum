package com.example.market.service;

import com.example.market.model.Order;
import com.example.market.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Optional<Order> findOrderByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> findAllOrderByStatusNot(String status) {
        return orderRepository.findAllByStatusNot(status);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
}
