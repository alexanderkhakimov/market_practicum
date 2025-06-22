package com.example.market.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;


@Data
@Table
public class Item {
    @Id
    private Long id;
    private String title;
    private int count;
    private BigDecimal price;
    private String imgPath;
    private String description;
}
