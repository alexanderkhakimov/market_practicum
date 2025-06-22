package com.example.market.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;


@Data
@Table
public class OrderItem {
    @Id
    private Long id;


    @Column("order_id")
    private Order order;

    @Column("item_id")
    private Item item;

    private int count;

    private BigDecimal priceAtOrder;


    public BigDecimal getTotalPrice() {
        return priceAtOrder.multiply(BigDecimal.valueOf(count));
    }

}
