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
public class InventoryContextTool {

    private final KnowledgeSearchService searchService;
    private final KnowledgeRenderer renderer;

    @Tool(name = "get_inventory_context",
          description = """
                  재고 관련 이커머스 도메인 지식 전체를 반환합니다.
                  재고 예약/확정/복구 단계, 오버셀링 방지, 동시성 제어, 멱등성, Saga 패턴을 포함합니다.
                  '재고 차감', '재고 로직', '재고 관리' 등의 요청 시 호출하세요.
                  """)
    public String getInventoryContext() {
        return renderer.renderAll(searchService.getByDomain("inventory"));
    }

    @Tool(name = "get_concurrency_strategy",
          description = """
                  재고 동시성 제어 전략 가이드를 반환합니다.
                  낙관락 / 비관락 / 분산락의 선택 기준, 장단점, 주의사항을 포함합니다.
                  '락', '동시성', 'Lock', '오버셀링', '동시 요청' 등의 요청 시 호출하세요.
                  """)
    public String getConcurrencyStrategy() {
        return renderer.renderAll(searchService.getByCategory("inventory", "concurrency"));
    }

    @Tool(name = "get_saga_pattern",
          description = """
                  주문-결제-재고 정합성을 위한 Saga 패턴 구현 가이드를 반환합니다.
                  정상 흐름 및 실패 시 보상 트랜잭션(Compensating Transaction) 흐름을 포함합니다.
                  'Saga', '보상 트랜잭션', '분산 트랜잭션', '정합성', 'Outbox' 요청 시 호출하세요.
                  """)
    public String getSagaPattern() {
        return renderer.renderAll(searchService.getByCategory("inventory", "consistency"));
    }

    @Tool(name = "get_idempotency_guide",
          description = """
                  재고 차감 API의 멱등성 설계 가이드를 반환합니다.
                  중복 요청 방어, idempotency key 설계 방법을 포함합니다.
                  '멱등성', '중복 요청', '이중 차감', 'idempotency' 요청 시 호출하세요.
                  """)
    public String getIdempotencyGuide() {
        return renderer.renderAll(searchService.getByCategory("inventory", "idempotency"));
    }

    @Tool(name = "get_inventory_checklist",
          description = """
                  AI가 재고 로직 구현 시 자주 빠뜨리는 패턴 체크리스트를 반환합니다.
                  코드 리뷰 또는 구현 완료 후 검토 용도로 사용하세요.
                  '체크리스트', '검토', '확인', 'checklist' 요청 시 호출하세요.
                  """)
    public String getInventoryChecklist() {
        return renderer.renderChecklist(searchService.getByCategory("inventory", "checklist"));
    }

    @Tool(name = "search_inventory_knowledge",
          description = """
                  키워드로 재고 도메인 지식을 검색합니다.
                  title, summary, tags, 섹션 내용에서 검색하며 관련도 순으로 반환합니다.
                  특정 주제에 대한 지식을 찾을 때 사용하세요.
                  """)
    public String searchInventoryKnowledge(
            @ToolParam(description = "검색 키워드 (예: 'redis', '낙관락', 'saga', '멱등성')") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "검색 키워드를 입력해주세요.";
        }
        var results = searchService.search(KnowledgeQuery.ofDomainAndKeyword("inventory", keyword));
        return results.isEmpty()
                ? "관련 재고 도메인 지식을 찾을 수 없습니다. 키워드: " + keyword
                : renderer.renderSearchResults(results);
    }
}
