package com.commerce.context_engine.core;

import java.util.List;

/**
 * 지식 저장소 추상화. YAML, DB, 벡터 검색 등 구현체는 이 인터페이스 뒤로 숨긴다.
 */
public interface KnowledgeRepository {

    List<KnowledgeEntry> findAll();

    List<KnowledgeEntry> findByDomain(String domain);

    List<KnowledgeEntry> findByCategory(String domain, String category);
}
