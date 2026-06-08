package com.commerce.context_engine.core;

/**
 * 지식 검색 조건.
 *
 * keyword: null 또는 blank → 키워드 필터 없음 (전체 반환)
 * domain:  null → 전체 도메인 검색
 * category: null → 카테고리 필터 없음
 * limit:   0 이하 → 제한 없음
 */
public record KnowledgeQuery(
        String keyword,
        String domain,
        String category,
        int limit
) {

    public static KnowledgeQuery ofKeyword(String keyword) {
        return new KnowledgeQuery(keyword, null, null, 0);
    }

    public static KnowledgeQuery ofDomainAndKeyword(String domain, String keyword) {
        return new KnowledgeQuery(keyword, domain, null, 0);
    }
}
