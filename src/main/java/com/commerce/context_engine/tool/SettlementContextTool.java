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
public class SettlementContextTool {

    private final KnowledgeSearchService searchService;
    private final KnowledgeRenderer renderer;

    @Tool(name = "get_settlement_timing",
          description = """
                  정산 시점과 정산 기준일 처리 가이드를 반환합니다.
                  구매 확정/배송 완료/결제 완료 기준 비교, 주말·공휴일 이월 처리를 포함합니다.
                  '정산 시점', '정산 기준일', '구매 확정 정산', '배송 완료 정산' 요청 시 호출하세요.
                  """)
    public String getSettlementTiming() {
        return renderer.renderAll(searchService.getByCategory("settlement", "timing"));
    }

    @Tool(name = "get_settlement_deduction",
          description = """
                  정산 공제 항목 처리 가이드를 반환합니다.
                  수수료, 반품 공제, 프로모션·쿠폰 분담금 계산 방법과 음수 정산 처리를 포함합니다.
                  '정산 공제', '수수료 공제', '반품 공제', '정산 금액 계산' 요청 시 호출하세요.
                  """)
    public String getSettlementDeduction() {
        return renderer.renderAll(searchService.getByCategory("settlement", "deduction"));
    }

    @Tool(name = "get_settlement_cycle",
          description = """
                  정산 주기와 배치 처리 가이드를 반환합니다.
                  일/주/월 정산 배치의 멱등성, 페이징, 실패 복구, 정산 잠금 처리를 포함합니다.
                  '정산 배치', '정산 주기', '배치 멱등성', '정산 스케줄러' 요청 시 호출하세요.
                  """)
    public String getSettlementCycle() {
        return renderer.renderAll(searchService.getByCategory("settlement", "cycle"));
    }

    @Tool(name = "get_settlement_integrity",
          description = """
                  정산 데이터 정합성 검증 가이드를 반환합니다.
                  금액·건수 정합성 검증, 중복·누락 확인, 타임존 주의사항을 포함합니다.
                  '정산 정합성', '정산 검증', '정산 타임존', '정산 누락' 요청 시 호출하세요.
                  """)
    public String getSettlementIntegrity() {
        return renderer.renderAll(searchService.getByCategory("settlement", "integrity"));
    }

    @Tool(name = "get_settlement_statement",
          description = """
                  정산 명세서 설계 가이드를 반환합니다.
                  명세를 정산 확정 시점의 요율·금액 스냅샷으로 박제하는 방법, 주문별 라인과
                  지급액 breakdown, 셀러 조회 API 소유권 검증, 명세-원장 정합성을 포함합니다.
                  '정산 명세서', '정산 내역', '셀러 정산 조회', '명세 스냅샷' 요청 시 호출하세요.
                  """)
    public String getSettlementStatement() {
        return renderer.renderAll(searchService.getByCategory("settlement", "statement"));
    }

    @Tool(name = "get_settlement_tax",
          description = """
                  정산 세무 처리 가이드를 반환합니다.
                  공급가액·부가세 분리, 면세/과세 상품 구분, 세금계산서 발행 시점,
                  개인 셀러 원천징수 처리를 포함합니다.
                  '정산 세무', '부가세', '세금계산서', '원천징수', '면세' 요청 시 호출하세요.
                  """)
    public String getSettlementTax() {
        return renderer.renderAll(searchService.getByCategory("settlement", "tax"));
    }

    @Tool(name = "get_settlement_hold",
          description = """
                  정산 보류 처리 가이드를 반환합니다.
                  분쟁·클레임·반품 중인 주문 제외, 셀러 단위/주문 단위 부분 보류,
                  보류 해제 후 이월, 사유·감사 로그를 포함합니다.
                  '정산 보류', '지급 보류', '분쟁 정산', '클레임 정산', 'chargeback' 요청 시 호출하세요.
                  """)
    public String getSettlementHold() {
        return renderer.renderAll(searchService.getByCategory("settlement", "hold"));
    }

    @Tool(name = "get_settlement_payout",
          description = """
                  정산 지급(실이체) 처리 가이드를 반환합니다.
                  정산 확정과 은행 이체 상태 분리, 이체 멱등성(이중 지급 방지),
                  이체 실패·불명확(UNCERTAIN) 처리, 최소 정산금액 이월을 포함합니다.
                  '정산 지급', '실이체', '이체 실패', '이중 지급', '미지급', '최소 정산금액' 요청 시 호출하세요.
                  """)
    public String getSettlementPayout() {
        return renderer.renderAll(searchService.getByCategory("settlement", "payout"));
    }

    @Tool(name = "get_settlement_checklist",
          description = """
                  정산 구현 시 AI가 자주 빠뜨리는 패턴 체크리스트를 반환합니다.
                  정산 기준일, 배치 멱등성, 공제 처리, 정합성 검증 항목을 포함합니다.
                  '정산 체크리스트', '정산 검토', '정산 구현 확인' 요청 시 호출하세요.
                  """)
    public String getSettlementChecklist() {
        return renderer.renderChecklist(searchService.getByCategory("settlement", "checklist"));
    }

    @Tool(name = "search_settlement_knowledge",
          description = """
                  키워드로 정산 도메인 지식을 검색합니다.
                  title, summary, tags, 섹션 내용에서 검색하며 관련도 순으로 반환합니다.
                  """)
    public String searchSettlementKnowledge(
            @ToolParam(description = "검색 키워드 (예: '배치', '공제', '타임존', '정합성')") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "검색 키워드를 입력해주세요.";
        }
        var results = searchService.search(KnowledgeQuery.ofDomainAndKeyword("settlement", keyword));
        return results.isEmpty()
                ? "관련 정산 도메인 지식을 찾을 수 없습니다. 키워드: " + keyword
                : renderer.renderSearchResults(results);
    }
}
