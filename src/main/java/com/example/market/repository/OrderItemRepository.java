package com.example.market.repository;

import com.example.market.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<Long, OrderItem> {
}
