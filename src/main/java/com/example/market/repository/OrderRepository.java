package com.example.market.repository;

import com.example.market.model.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends R2dbcRepository< Order, Long> {
    Optional<Order> findByStatus(String status);
    List<Order> findAllByStatusNot(String status);
}
