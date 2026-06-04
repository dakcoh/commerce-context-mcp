package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.settlement.SettlementKnowledgeProperties;
import com.commerce.context_engine.domain.settlement.SettlementKnowledgeProperties.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.commerce.context_engine.service.KnowledgeSearchSupport.containsNormalized;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.hasSearchKeyword;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.missingKeywordMessage;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.normalize;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.safeList;

@Service
@RequiredArgsConstructor
public class SettlementKnowledgeService {

    private final SettlementKnowledgeProperties properties;

    /** 정산 시점 및 기준일 처리 */
    public String getSettlementTiming() {
        return format(filterByCategory("timing"));
    }

    /** 정산 공제 항목 처리 */
    public String getSettlementDeduction() {
        return format(filterByCategory("deduction"));
    }

    /** 정산 주기와 배치 처리 */
    public String getSettlementCycle() {
        return format(filterByCategory("cycle"));
    }

    /** 정산 데이터 정합성 검증 */
    public String getSettlementIntegrity() {
        return format(filterByCategory("integrity"));
    }

    /** AI가 자주 빠뜨리는 정산 패턴 체크리스트 */
    public String getChecklist() {
        return filterByCategory("checklist").stream()
                .map(Item::getContent)
                .collect(Collectors.joining("\n"));
    }

    /** 키워드로 정산 지식 검색 */
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
                .toList();

        if (matched.isEmpty()) {
            return "관련 정산 도메인 지식을 찾을 수 없습니다. 키워드: " + keyword;
        }
        return format(matched);
    }

    private List<Item> filterByCategory(String category) {
        return safeList(properties.getItems()).stream()
                .filter(i -> category.equals(i.getCategory()))
                .toList();
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
