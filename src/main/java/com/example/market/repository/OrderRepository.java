package com.example.market.repository;

import com.example.market.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Long, Order> {
}
