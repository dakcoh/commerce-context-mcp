package com.commerce.context_engine.tool;

import com.commerce.context_engine.core.KnowledgeQuery;
import com.commerce.context_engine.core.KnowledgeRenderer;
import com.commerce.context_engine.core.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnifiedSearchTool {

    private final KnowledgeSearchService searchService;
    private final KnowledgeRenderer renderer;

    @Tool(name = "search_all_knowledge",
          description = """
                  도메인 구분 없이 전체 이커머스 지식을 통합 검색합니다.
                  재고·결제·정산·쿠폰·범용 이커머스·Java Spring 구현 지식을 한 번에 탐색합니다.
                  어느 도메인인지 모르거나 여러 도메인에 걸친 주제를 검색할 때 사용하세요.
                  결과는 관련도 점수(score) 내림차순으로 반환됩니다.
                  예: '멱등성', 'outbox', '동시성', '타임존', '부분 취소', 'redis'
                  """)
    public String searchAllKnowledge(
            @ToolParam(description = "검색 키워드 (예: '멱등성', 'outbox', '동시성', 'redis', '부분 취소', '타임존')") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "검색 키워드를 입력해주세요.";
        }
        var results = searchService.search(KnowledgeQuery.ofKeyword(keyword));
        return results.isEmpty()
                ? "관련 지식을 찾을 수 없습니다. 키워드: " + keyword
                : renderer.renderSearchResults(results);
    }
}
