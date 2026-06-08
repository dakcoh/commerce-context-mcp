package com.commerce.context_engine.core;

import java.util.List;

/**
 * 지식을 MCP 응답용 Markdown 문자열로 변환하는 렌더러 인터페이스.
 * Tool 클래스는 이 인터페이스에만 의존하며, 포맷 구현은 렌더러에 위임한다.
 *
 * 단일 항목 렌더링(render)은 구현체 내부 관심사이므로 이 계약에 포함하지 않는다.
 */
public interface KnowledgeRenderer {

    String renderAll(List<KnowledgeEntry> entries);

    String renderSearchResults(List<KnowledgeSearchResult> results);

    String renderChecklist(List<KnowledgeEntry> entries);
}
