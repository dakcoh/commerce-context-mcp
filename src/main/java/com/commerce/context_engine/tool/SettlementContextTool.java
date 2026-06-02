package com.commerce.context_engine.tool;

import com.commerce.context_engine.service.SettlementKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementContextTool {

    private final SettlementKnowledgeService knowledgeService;

    @Tool(name = "get_settlement_timing",
          description = """
                  정산 시점과 정산 기준일 처리 가이드를 반환합니다.
                  구매 확정/배송 완료/결제 완료 기준 비교, 주말·공휴일 이월 처리를 포함합니다.
                  '정산 시점', '정산 기준일', '구매 확정 정산', '배송 완료 정산' 요청 시 호출하세요.
                  """)
    public String getSettlementTiming() {
        return knowledgeService.getSettlementTiming();
    }

    @Tool(name = "get_settlement_deduction",
          description = """
                  정산 공제 항목 처리 가이드를 반환합니다.
                  수수료, 반품 공제, 프로모션·쿠폰 분담금 계산 방법과 음수 정산 처리를 포함합니다.
                  '정산 공제', '수수료 공제', '반품 공제', '정산 금액 계산' 요청 시 호출하세요.
                  """)
    public String getSettlementDeduction() {
        return knowledgeService.getSettlementDeduction();
    }

    @Tool(name = "get_settlement_cycle",
          description = """
                  정산 주기와 배치 처리 가이드를 반환합니다.
                  일/주/월 정산 배치의 멱등성, 페이징, 실패 복구, 정산 잠금 처리를 포함합니다.
                  '정산 배치', '정산 주기', '배치 멱등성', '정산 스케줄러' 요청 시 호출하세요.
                  """)
    public String getSettlementCycle() {
        return knowledgeService.getSettlementCycle();
    }

    @Tool(name = "get_settlement_integrity",
          description = """
                  정산 데이터 정합성 검증 가이드를 반환합니다.
                  금액·건수 정합성 검증, 중복·누락 확인, 타임존 주의사항을 포함합니다.
                  '정산 정합성', '정산 검증', '정산 타임존', '정산 누락' 요청 시 호출하세요.
                  """)
    public String getSettlementIntegrity() {
        return knowledgeService.getSettlementIntegrity();
    }

    @Tool(name = "get_settlement_checklist",
          description = """
                  정산 구현 시 AI가 자주 빠뜨리는 패턴 체크리스트를 반환합니다.
                  정산 기준일, 배치 멱등성, 공제 처리, 정합성 검증 항목을 포함합니다.
                  '정산 체크리스트', '정산 검토', '정산 구현 확인' 요청 시 호출하세요.
                  """)
    public String getSettlementChecklist() {
        return knowledgeService.getChecklist();
    }

    @Tool(name = "search_settlement_knowledge",
          description = """
                  키워드로 정산 도메인 지식을 검색합니다.
                  title, content, tags에서 검색하며 관련 항목을 반환합니다.
                  """)
    public String searchSettlementKnowledge(
            @ToolParam(description = "검색 키워드 (예: '배치', '공제', '타임존', '정합성')") String keyword) {
        return knowledgeService.search(keyword);
    }
}
