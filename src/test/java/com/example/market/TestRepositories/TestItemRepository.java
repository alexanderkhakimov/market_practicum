package com.example.market.TestRepositories;

import com.example.market.model.Item;
import com.example.market.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DataJpaTest
public class TestItemRepository {
    @Autowired
    ItemRepository itemRepository;

    @Test
    void saveItem() {
        Item item = new Item();
        item.setTitle("Test Item");
        item.setPrice(new BigDecimal("99.99"));
        item.setCount(323);
        itemRepository.save(item);

        Item newItem = itemRepository.findByTitle("Test Item");
        assertEquals("Test Item", newItem.getTitle());
    }
}
