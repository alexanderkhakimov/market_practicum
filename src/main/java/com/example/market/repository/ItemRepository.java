package com.example.market.repository;

import com.example.market.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT i FROM Item i WHERE i.title LIKE %:search% OR i.description LIKE %:search%")
    Page<Item> findBySearch(String search, Pageable pageable);

    Item findByTitle(String title);
}
