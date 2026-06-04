package com.commerce.context_engine.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventoryKnowledgeServiceTest {

    @Autowired
    InventoryKnowledgeService service;

    @Test
    void getInventoryContext_returnsAllItems() {
        String result = service.getInventoryContext();
        assertThat(result).isNotBlank();
        assertThat(result).contains("재고 예약");
        assertThat(result).contains("낙관락");
        assertThat(result).contains("Saga");
    }

    @Test
    void getConcurrencyStrategy_returnsConcurrencyItemsOnly() {
        String result = service.getConcurrencyStrategy();
        assertThat(result).contains("낙관락");
        assertThat(result).contains("비관락");
        assertThat(result).contains("분산락");
        // 다른 카테고리 항목 포함 X
        assertThat(result).doesNotContain("멱등성 설계");
    }

    @Test
    void getSagaPattern_returnsConsistencyItems() {
        String result = service.getSagaPattern();
        assertThat(result).contains("Saga");
        assertThat(result).contains("보상");
    }

    @Test
    void getIdempotencyGuide_returnsIdempotencyItems() {
        String result = service.getIdempotencyGuide();
        assertThat(result).contains("idempotency");
        assertThat(result).contains("중복");
    }

    @Test
    void getChecklist_returnsChecklistItems() {
        String result = service.getChecklist();
        assertThat(result).contains("[ ]");
        assertThat(result).contains("동시성");
    }

    @Test
    void search_byKeyword_returnsMatchingItems() {
        String result = service.search("redis");
        assertThat(result).contains("분산락");
        assertThat(result).doesNotContain("낙관락 (Optimistic Lock)");
    }

    @Test
    void search_unknownKeyword_returnsNotFoundMessage() {
        String result = service.search("존재하지않는키워드xyz");
        assertThat(result).contains("찾을 수 없습니다");
    }

    @Test
    void search_blankKeyword_returnsKeywordRequiredMessage() {
        assertThat(service.search("   ")).contains("search keyword");
    }

    @Test
    void search_nullKeyword_returnsKeywordRequiredMessage() {
        assertThat(service.search(null)).contains("search keyword");
    }
}
