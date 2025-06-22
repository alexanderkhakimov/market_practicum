package com.example.market.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Table(name = "orders")
public class Order {
    @Id
    private Long id;
    private String status;

    private List<OrderItem> orderItems = new ArrayList<>();

    public Mono<BigDecimal> totalSum() {
        return Mono.justOrEmpty(orderItems)
                .flatMapMany(Flux::fromIterable)
                .map(OrderItem::getPriceAtOrder)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
