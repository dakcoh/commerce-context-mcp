package com.commerce.context_engine.repository;

import com.commerce.context_engine.core.KnowledgeEntry;
import com.commerce.context_engine.domain.SimpleKnowledgeItem;
import com.commerce.context_engine.domain.commerce.CommerceKnowledgeProperties;
import com.commerce.context_engine.domain.springcommerce.SpringCommerceKnowledgeProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class KnowledgeEntryMapper {

    private KnowledgeEntryMapper() {
    }

    /**
     * 단순 도메인(inventory / payment / settlement / coupon) 공통 변환.
     * domain 이름은 호출 측에서 명시적으로 전달한다.
     */
    static KnowledgeEntry fromSimple(String domain, SimpleKnowledgeItem item) {
        return structured(domain, item.getId(), item.getCategory(), item.getTitle(),
                item.getSummary(), item.getGuidance(), item.getAvoidPatterns(),
                item.getChecklist(), item.getTags());
    }

    static KnowledgeEntry from(CommerceKnowledgeProperties.Item item) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("invariants", safeList(item.getInvariants()));
        sections.put("workflow", safeList(item.getWorkflow()));
        sections.put("technical-guidance", safeList(item.getTechnicalGuidance()));
        sections.put("failure-scenarios", safeList(item.getFailureScenarios()));

        return new KnowledgeEntry(
                "commerce",
                item.getId(),
                item.getCategory(),
                item.getTitle(),
                item.getSummary(),
                item.getBusinessContext(),
                sections,
                safeList(item.getChecklist()),
                safeList(item.getTags())
        );
    }

    static KnowledgeEntry from(SpringCommerceKnowledgeProperties.Item item) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("spring-guidance", safeList(item.getSpringGuidance()));
        sections.put("avoid-patterns", safeList(item.getAvoidPatterns()));

        return new KnowledgeEntry(
                "spring-commerce",
                item.getId(),
                item.getCategory(),
                item.getTitle(),
                item.getSummary(),
                item.getBusinessContext(),
                sections,
                safeList(item.getChecklist()),
                safeList(item.getTags())
        );
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static KnowledgeEntry structured(String domain, String id, String category,
                                             String title, String summary,
                                             List<String> guidance, List<String> avoidPatterns,
                                             List<String> checklist, List<String> tags) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        if (!safeList(guidance).isEmpty()) {
            sections.put("guidance", safeList(guidance));
        }
        if (!safeList(avoidPatterns).isEmpty()) {
            sections.put("avoid-patterns", safeList(avoidPatterns));
        }
        return new KnowledgeEntry(domain, id, category, title,
                summary, null, sections, safeList(checklist), safeList(tags));
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }
}
