package com.commerce.context_engine.core;

import java.util.List;

/**
 * 지식 검색 서비스 인터페이스.
 * MCP Tool은 이 인터페이스만 의존한다.
 */
public interface KnowledgeSearchService {

    List<KnowledgeSearchResult> search(KnowledgeQuery query);

    List<KnowledgeEntry> getByDomain(String domain);

    List<KnowledgeEntry> getByCategory(String domain, String category);
}
