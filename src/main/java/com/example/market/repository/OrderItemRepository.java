package com.example.market.repository;

import com.example.market.model.OrderItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;
@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
    Flux<OrderItem> findByOrderId(Long orderId);

    Flux<OrderItem> findByItemId(Long itemId);

}
