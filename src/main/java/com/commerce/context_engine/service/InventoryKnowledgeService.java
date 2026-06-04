package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.inventory.InventoryKnowledgeProperties;
import com.commerce.context_engine.domain.inventory.InventoryKnowledgeProperties.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.commerce.context_engine.service.KnowledgeSearchSupport.containsNormalized;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.hasSearchKeyword;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.missingKeywordMessage;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.normalize;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.safeList;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryKnowledgeService {

    private final InventoryKnowledgeProperties properties;

    /** 전체 재고 도메인 컨텍스트 (모든 항목) */
    public String getInventoryContext() {
        return format(properties.getItems());
    }

    /** 동시성 제어 전략 (낙관락 / 비관락 / 분산락) */
    public String getConcurrencyStrategy() {
        return format(filterByCategory("concurrency"));
    }

    /** Saga 패턴 / 보상 트랜잭션 */
    public String getSagaPattern() {
        return format(filterByCategory("consistency"));
    }

    /** 멱등성 설계 */
    public String getIdempotencyGuide() {
        return format(filterByCategory("idempotency"));
    }

    /** AI가 자주 빠뜨리는 패턴 체크리스트 */
    public String getChecklist() {
        return filterByCategory("checklist").stream()
                .map(Item::getContent)
                .collect(Collectors.joining("\n"));
    }

    /**
     * 키워드로 지식 항목 검색.
     * title, content, tags 중 하나라도 keyword를 포함하면 반환한다.
     */
    public String search(String keyword) {
        if (!hasSearchKeyword(keyword)) {
            return missingKeywordMessage();
        }

        String lower = normalize(keyword);
        List<Item> matched = safeList(properties.getItems()).stream()
                .filter(item ->
                        containsNormalized(item.getTitle(), lower)
                        || containsNormalized(item.getContent(), lower)
                        || safeList(item.getTags()).stream().anyMatch(t -> containsNormalized(t, lower)))
                .collect(Collectors.toList());

        if (matched.isEmpty()) {
            return "관련 재고 도메인 지식을 찾을 수 없습니다. 키워드: " + keyword;
        }
        return format(matched);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<Item> filterByCategory(String category) {
        return safeList(properties.getItems()).stream()
                .filter(i -> category.equals(i.getCategory()))
                .collect(Collectors.toList());
    }

    private String format(List<Item> items) {
        return safeList(items).stream()
                .map(i -> "## " + i.getTitle() + "\n" + normalizeContent(i.getContent()))
                .collect(Collectors.joining("\n\n"));
    }

    private String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }
}
