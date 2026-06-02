package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.settlement.SettlementKnowledgeProperties;
import com.commerce.context_engine.domain.settlement.SettlementKnowledgeProperties.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        String lower = keyword.toLowerCase();
        List<Item> matched = properties.getItems().stream()
                .filter(item ->
                        item.getTitle().toLowerCase().contains(lower)
                        || item.getContent().toLowerCase().contains(lower)
                        || item.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lower)))
                .collect(Collectors.toList());

        if (matched.isEmpty()) {
            return "관련 정산 도메인 지식을 찾을 수 없습니다. 키워드: " + keyword;
        }
        return format(matched);
    }

    private List<Item> filterByCategory(String category) {
        return properties.getItems().stream()
                .filter(i -> category.equals(i.getCategory()))
                .collect(Collectors.toList());
    }

    private String format(List<Item> items) {
        return items.stream()
                .map(i -> "## " + i.getTitle() + "\n" + i.getContent().trim())
                .collect(Collectors.joining("\n\n"));
    }
}
