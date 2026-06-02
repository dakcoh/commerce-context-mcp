package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.springcommerce.SpringCommerceKnowledgeProperties;
import com.commerce.context_engine.domain.springcommerce.SpringCommerceKnowledgeProperties.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SpringCommerceKnowledgeService {

    private final SpringCommerceKnowledgeProperties properties;

    public String getSpringImplementationContext() {
        return format(properties.getItems());
    }

    public String getChecklist() {
        return properties.getItems().stream()
                .map(this::formatChecklist)
                .collect(Collectors.joining("\n\n"));
    }

    public String search(String keyword) {
        String normalizedKeyword = normalize(keyword);
        List<Item> matched = properties.getItems().stream()
                .filter(item -> searchableValues(item)
                        .map(this::normalize)
                        .anyMatch(value -> value.contains(normalizedKeyword)))
                .toList();

        if (matched.isEmpty()) {
            return "관련 Java Spring 이커머스 구현 지식을 찾을 수 없습니다. 키워드: " + keyword;
        }
        return format(matched);
    }

    private Stream<String> searchableValues(Item item) {
        return Stream.concat(
                Stream.of(item.getId(), item.getCategory(), item.getTitle(), item.getSummary(),
                        item.getBusinessContext()),
                Stream.of(item.getSpringGuidance(), item.getAvoidPatterns(), item.getChecklist(), item.getTags())
                        .flatMap(List::stream));
    }

    private String format(List<Item> items) {
        return items.stream().map(this::format).collect(Collectors.joining("\n\n---\n\n"));
    }

    private String format(Item item) {
        return """
                ## %s
                - knowledgeId: `%s`
                - category: `%s`
                - summary: %s

                ### 이커머스 맥락
                %s

                ### Java Spring 구현 가이드
                %s

                ### 피해야 할 패턴
                %s

                ### 검토 체크리스트
                %s
                """.formatted(
                item.getTitle(), item.getId(), item.getCategory(), item.getSummary(),
                item.getBusinessContext().trim(), bulletList(item.getSpringGuidance()),
                bulletList(item.getAvoidPatterns()), checkboxList(item.getChecklist()))
                .trim();
    }

    private String formatChecklist(Item item) {
        return "## " + item.getTitle() + "\n"
                + "- knowledgeId: `" + item.getId() + "`\n"
                + checkboxList(item.getChecklist());
    }

    private String bulletList(List<String> items) {
        return items.stream().map(item -> "- " + item).collect(Collectors.joining("\n"));
    }

    private String checkboxList(List<String> items) {
        return items.stream().map(item -> "- [ ] " + item).collect(Collectors.joining("\n"));
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
