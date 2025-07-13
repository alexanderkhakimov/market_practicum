package com.example.market.repository;

import com.example.market.model.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface OrderRepository extends R2dbcRepository< Order, Long> {
    Mono<Order> findByStatus(String status);

    Flux<Order> findAllByStatusNot(String status);

}
