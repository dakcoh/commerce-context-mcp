package com.commerce.context_engine.service;

import com.commerce.context_engine.core.KnowledgeQuery;
import com.commerce.context_engine.core.KnowledgeSearchResult;
import com.commerce.context_engine.core.KnowledgeSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DefaultKnowledgeSearchServiceTest {

    @Autowired
    KnowledgeSearchService searchService;

    // ── keyword 검색 ──────────────────────────────────────────────────────────

    @Test
    void search_withKeyword_returnsMatchedResults() {
        List<KnowledgeSearchResult> results = searchService.search(KnowledgeQuery.ofKeyword("멱등성"));

        assertThat(results).isNotEmpty();
        results.forEach(r -> assertThat(r.matchedFields()).isNotEmpty());
    }

    @Test
    void search_withKeyword_isSortedByScoreDesc() {
        List<KnowledgeSearchResult> results = searchService.search(KnowledgeQuery.ofKeyword("재고"));

        assertThat(results).isNotEmpty();
        for (int i = 0; i < results.size() - 1; i++) {
            assertThat(results.get(i).score()).isGreaterThanOrEqualTo(results.get(i + 1).score());
        }
    }

    @Test
    void search_titleMatch_scoresHigherThanContentMatch() {
        List<KnowledgeSearchResult> results = searchService.search(KnowledgeQuery.ofKeyword("낙관락"));

        assertThat(results).isNotEmpty();
        KnowledgeSearchResult top = results.get(0);
        assertThat(top.matchedFields()).contains("title");
        assertThat(top.score()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void search_blankKeyword_returnsAllCandidatesWithScoreZero() {
        List<KnowledgeSearchResult> results = searchService.search(
                new KnowledgeQuery("  ", "inventory", null, 0));

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.score() == 0);
        assertThat(results).allMatch(r -> r.matchedFields().isEmpty());
    }

    @Test
    void search_noMatch_returnsEmptyList() {
        List<KnowledgeSearchResult> results = searchService.search(
                KnowledgeQuery.ofKeyword("존재하지않는키워드xyzabc"));

        assertThat(results).isEmpty();
    }

    // ── domain 필터 ───────────────────────────────────────────────────────────

    @Test
    void search_withDomain_returnsOnlyThatDomain() {
        List<KnowledgeSearchResult> results = searchService.search(
                KnowledgeQuery.ofDomainAndKeyword("payment", "멱등성"));

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> "payment".equals(r.entry().domain()));
    }

    @Test
    void search_crossDomain_sameKeyword_returnsBothDomains() {
        List<KnowledgeSearchResult> results = searchService.search(KnowledgeQuery.ofKeyword("멱등성"));

        assertThat(results.stream().map(r -> r.entry().domain()).distinct())
                .hasSizeGreaterThan(1);
    }

    // ── limit ─────────────────────────────────────────────────────────────────

    @Test
    void search_withLimit_respectsLimit() {
        List<KnowledgeSearchResult> results = searchService.search(
                new KnowledgeQuery("재고", null, null, 3));

        assertThat(results).hasSizeLessThanOrEqualTo(3);
    }

    // ── getByDomain / getByCategory ───────────────────────────────────────────

    @Test
    void getByDomain_returnsEntriesForDomain() {
        assertThat(searchService.getByDomain("inventory")).isNotEmpty();
        assertThat(searchService.getByDomain("commerce")).isNotEmpty();
    }

    @Test
    void getByCategory_returnsOnlyMatchingCategory() {
        var entries = searchService.getByCategory("inventory", "concurrency");

        assertThat(entries).isNotEmpty();
        assertThat(entries).allMatch(e -> "concurrency".equals(e.category()));
    }

    // ── matchedFields 정확성 ──────────────────────────────────────────────────

    @Test
    void search_commerce_matchedFields_includesSections() {
        List<KnowledgeSearchResult> results = searchService.search(
                KnowledgeQuery.ofDomainAndKeyword("commerce", "재고"));

        assertThat(results).isNotEmpty();
        boolean hasSectionMatch = results.stream()
                .anyMatch(r -> r.matchedFields().stream().anyMatch(f -> f.startsWith("sections.")));
        assertThat(hasSectionMatch).isTrue();
    }
}
