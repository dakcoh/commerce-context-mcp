package com.commerce.context_engine.core;

import java.util.List;
import java.util.Map;

/**
 * 도메인과 YAML 스키마에 무관한 정규화 지식 단위.
 *
 * sections: 도메인별 명명된 리스트 섹션. 삽입 순서를 보장하는 Map(LinkedHashMap)으로 전달할 것.
 *   - commerce:        invariants / workflow / technical-guidance / failure-scenarios
 *   - spring-commerce: spring-guidance / avoid-patterns
 *   - simple 도메인:   guidance / avoid-patterns (checklist 카테고리는 빈 Map)
 *
 * content: commerce/spring-commerce 의 business-context 값. simple 도메인은 항상 null.
 * checklist: simple 도메인의 체크리스트는 category="checklist" 항목에서 관리.
 */
public record KnowledgeEntry(
        String domain,
        String id,
        String category,
        String title,
        String summary,
        String content,
        Map<String, List<String>> sections,
        List<String> checklist,
        List<String> tags
) {
}
