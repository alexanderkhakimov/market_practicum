package com.example.market.service;

import com.example.market.model.Item;
import com.example.market.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Page<Item> findBySearch(String search, Pageable pageable) {
        return itemRepository.findBySearch(search, pageable);
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }
}
