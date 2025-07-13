package com.example.market.service;

import com.example.market.model.Item;
import com.example.market.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Flux<Item> findBySearch(String search, Pageable pageable) {
        String sortField = pageable.getSort().get().findFirst()
                .map(Sort.Order::getProperty)
                .orElse("id");
        String sortDirection = pageable.getSort().get().findFirst()
                .map(order -> order.getDirection() == Sort.Direction.ASC ? "ASC" : "DESC")
                .orElse("ASC");
        return itemRepository.findBySearch(search, pageable.getPageSize(), pageable.getOffset(), sortField, sortDirection);
    }
    public Mono<Item> findById(Long id) {
        return itemRepository.findById(id);
    }
    public Mono<Long> countBySearch(String search){
        return itemRepository.countBySearch(search);
    }
}
