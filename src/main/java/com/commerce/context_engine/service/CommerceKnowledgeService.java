package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.commerce.CommerceKnowledgeProperties;
import com.commerce.context_engine.domain.commerce.CommerceKnowledgeProperties.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.commerce.context_engine.service.KnowledgeSearchSupport.containsNormalized;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.hasSearchKeyword;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.missingKeywordMessage;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.normalize;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.safeList;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.safeStream;

@Service
@RequiredArgsConstructor
public class CommerceKnowledgeService {

    private final CommerceKnowledgeProperties properties;

    public String getFoundationContext() {
        return format(safeList(properties.getItems()));
    }

    public String getChecklist() {
        return safeList(properties.getItems()).stream()
                .map(this::formatChecklist)
                .collect(Collectors.joining("\n\n"));
    }

    public String search(String keyword) {
        if (!hasSearchKeyword(keyword)) {
            return missingKeywordMessage();
        }

        String normalizedKeyword = normalize(keyword);
        List<Item> matched = safeList(properties.getItems()).stream()
                .filter(item -> searchableValues(item)
                        .anyMatch(value -> containsNormalized(value, normalizedKeyword)))
                .toList();

        if (matched.isEmpty()) {
            return "관련 범용 이커머스 지식을 찾을 수 없습니다. 키워드: " + keyword;
        }
        return format(matched);
    }

    private Stream<String> searchableValues(Item item) {
        return Stream.concat(
                Stream.of(item.getId(), item.getCategory(), item.getTitle(), item.getSummary(),
                        item.getBusinessContext()),
                Stream.of(item.getInvariants(), item.getWorkflow(), item.getTechnicalGuidance(),
                                item.getFailureScenarios(), item.getChecklist(), item.getTags())
                        .flatMap(KnowledgeSearchSupport::safeStream));
    }

    private String format(List<Item> items) {
        return safeList(items).stream()
                .map(this::format)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    private String format(Item item) {
        return """
                ## %s
                - knowledgeId: `%s`
                - category: `%s`
                - summary: %s

                ### 이커머스 맥락
                %s

                ### 반드시 지켜야 할 원칙
                %s

                ### 권장 흐름
                %s

                ### 기술 구현 참고
                %s

                ### 실패 시나리오
                %s

                ### 검토 체크리스트
                %s
                """.formatted(
                item.getTitle(),
                item.getId(),
                item.getCategory(),
                item.getSummary(),
                trim(item.getBusinessContext()),
                bulletList(item.getInvariants()),
                numberedList(item.getWorkflow()),
                bulletList(item.getTechnicalGuidance()),
                bulletList(item.getFailureScenarios()),
                checkboxList(item.getChecklist()))
                .trim();
    }

    private String formatChecklist(Item item) {
        return "## " + item.getTitle() + "\n"
                + "- knowledgeId: `" + item.getId() + "`\n"
                + checkboxList(item.getChecklist());
    }

    private String bulletList(List<String> items) {
        return safeStream(items).map(item -> "- " + item).collect(Collectors.joining("\n"));
    }

    private String numberedList(List<String> items) {
        List<String> safeItems = safeList(items);
        return java.util.stream.IntStream.range(0, safeItems.size())
                .mapToObj(index -> (index + 1) + ". " + safeItems.get(index))
                .collect(Collectors.joining("\n"));
    }

    private String checkboxList(List<String> items) {
        return safeStream(items).map(item -> "- [ ] " + item).collect(Collectors.joining("\n"));
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
