package com.example.market.repository;

import com.example.market.model.Item;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {

    @Cacheable(value = "items", key = "#search + '-' + #limit + '-' + #offset + '-' + #sortField + '-' + #sortDirection")
    @Query("SELECT * FROM item WHERE title LIKE CONCAT('%', :search, '%') " +
            "OR description LIKE CONCAT('%', :search, '%') " +
            "ORDER BY :sortField :sortDirection " +
            "LIMIT :limit OFFSET :offset")
    Flux<Item> findBySearch(@Param("search") String search,
                            @Param("limit") int limit,
                            @Param("offset") long offset,
                            @Param("sortField") String sortField,
                            @Param("sortDirection") String sortDirection);

    @Cacheable(value = "items", key = "#search + '-count'")
    @Query("SELECT COUNT(*) FROM item WHERE title LIKE CONCAT('%', :search, '%') " +
            "OR description LIKE CONCAT('%', :search, '%')")
    Mono<Long> countBySearch(@Param("search") String search);

    @Override
    @CachePut(value = "items", key = "#entity.id")
    Mono<Item> save(Item entity);

    @Override
    @CacheEvict(value = "items", key = "#id")
    Mono<Void> deleteById(Long id);

    @Override
    @CacheEvict(value = "items", allEntries = true)
    Mono<Void> deleteAll();
}