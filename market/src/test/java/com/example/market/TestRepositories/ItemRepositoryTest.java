package com.example.market.TestRepositories;

import com.example.market.model.Item;
import com.example.market.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = LiquibaseAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password="
})
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        // Очистка базы и добавление тестовых данных
        itemRepository.deleteAll()
                .thenMany(createTestItems())
                .flatMap(itemRepository::save)
                .blockLast();
    }

    private Flux<Item> createTestItems() {
        Item item1 = new Item();
        item1.setTitle("Смартфон");
        item1.setDescription("Новый");
        item1.setPrice(new BigDecimal("999.99"));

        Item item2 = new Item();
        item2.setTitle("Ноутбук ");
        item2.setDescription(" ноутбук");
        item2.setPrice(new BigDecimal("1499.99"));

        Item item3 = new Item();
        item3.setTitle("Наушники");
        item3.setDescription("наушники");
        item3.setPrice(new BigDecimal("199.99"));

        return Flux.just(item1, item2, item3);
    }

    @Test
    void countBySearch_shouldReturnCorrectCount() {
        Mono<Long> count = itemRepository.countBySearch("ноутбук");

        Long result = count.block();

        assertNotNull(result);
        assertEquals(1, result);
    }

//    @Test
//    void testFindBySearchCaching() {
//        String search = "phone";
//        int limit = 10;
//        long offset = 0;
//        String sortField = "price";
//        String sortDirection = "ASC";
//
//        // Первый вызов: запрос к базе и кэширование
//        StepVerifier.create(itemRepository.findBySearch(search, limit, offset, sortField, sortDirection))
//                .expectNextCount(1) // Укажите ожидаемое количество
//                .verifyComplete();
//
//        // Второй вызов: данные из кэша
//        StepVerifier.create(itemRepository.findBySearch(search, limit, offset, sortField, sortDirection))
//                .expectNextCount(1)
//                .verifyComplete();
//    }
}