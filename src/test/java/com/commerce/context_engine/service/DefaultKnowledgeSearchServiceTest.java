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

    @Test
    void search_multiWordKeyword_matchesEachTokenIndependently() {
        // "웹훅 중복 결제"는 한 항목에 통째로 들어있지 않지만, 토큰별 매칭으로 결과가 노출돼야 한다
        List<KnowledgeSearchResult> results = searchService.search(
                KnowledgeQuery.ofKeyword("웹훅 중복 결제"));

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.matchedFields().isEmpty() == false);
    }

    @Test
    void search_whitespaceInsensitive_matchesSpacedContent() {
        // 사용자가 띄어쓰기 없이 "부분취소"로 검색해도 "부분 취소" 지식이 매칭돼야 한다
        List<KnowledgeSearchResult> spaced = searchService.search(KnowledgeQuery.ofKeyword("부분 취소"));
        List<KnowledgeSearchResult> joined = searchService.search(KnowledgeQuery.ofKeyword("부분취소"));

        assertThat(spaced).isNotEmpty();
        assertThat(joined).isNotEmpty();
    }

    @Test
    void search_singleToken_scoringUnchanged() {
        // 단일 토큰 검색은 기존 스코어링과 동일해야 한다 (title 매칭 시 최소 5점)
        List<KnowledgeSearchResult> results = searchService.search(KnowledgeQuery.ofKeyword("낙관락"));

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).matchedFields()).contains("title");
        assertThat(results.get(0).score()).isGreaterThanOrEqualTo(5);
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
