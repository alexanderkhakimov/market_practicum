package com.example.market.repository;

import com.example.market.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE i.title LIKE %:search% OR i.description LIKE %:search%")
    Mono<Page<Item>> findBySearch(String search, Pageable pageable);

    Mono<Item> findByTitle(String title);
}
