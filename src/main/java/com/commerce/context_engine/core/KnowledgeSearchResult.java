package com.commerce.context_engine.core;

import java.util.List;

/**
 * 검색 결과 단위. score는 단순 규칙 기반 가중치 합산:
 *   title 일치    +5
 *   tag 일치      +4
 *   category 일치 +3
 *   summary 일치  +2
 *   content/sections/checklist 일치 +1
 */
public record KnowledgeSearchResult(
        KnowledgeEntry entry,
        List<String> matchedFields,
        int score
) {
}
