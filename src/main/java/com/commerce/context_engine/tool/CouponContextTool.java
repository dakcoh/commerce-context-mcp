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
public class CouponContextTool {

    private final KnowledgeSearchService searchService;
    private final KnowledgeRenderer renderer;

    @Tool(name = "get_coupon_validation_guide",
          description = """
                  쿠폰 유효성 검증 가이드를 반환합니다.
                  유효 기간, 사용 대상 조건, 중복 사용, 1인 한도, 발급 대상 검증 5가지와 동시성 처리를 포함합니다.
                  '쿠폰 검증', '쿠폰 유효성', '쿠폰 사용 조건', '쿠폰 중복 사용' 요청 시 호출하세요.
                  """)
    public String getCouponValidationGuide() {
        return renderer.renderAll(searchService.getByCategory("coupon", "validation"));
    }

    @Tool(name = "get_coupon_discount_calculation",
          description = """
                  쿠폰 할인 금액 계산 가이드를 반환합니다.
                  정률/정액 할인 캡 처리, 복수 쿠폰 적용 순서, 포인트 병행 사용, 부분 취소 시 역산을 포함합니다.
                  '쿠폰 할인 계산', '정률 할인', '정액 할인', '쿠폰 할인 금액', '복수 쿠폰' 요청 시 호출하세요.
                  """)
    public String getCouponDiscountCalculation() {
        return renderer.renderAll(searchService.getByCategory("coupon", "calculation"));
    }

    @Tool(name = "get_coupon_issuance_guide",
          description = """
                  선착순 쿠폰 발급 동시성 제어 가이드를 반환합니다.
                  Redis 원자적 연산, DB 비관락, 메시지 큐 비동기 발급 전략과 이중 발급 방어를 포함합니다.
                  '선착순 쿠폰', '쿠폰 발급', '쿠폰 재고', '쿠폰 동시성', '오버이슈' 요청 시 호출하세요.
                  """)
    public String getCouponIssuanceGuide() {
        return renderer.renderAll(searchService.getByCategory("coupon", "issuance"));
    }

    @Tool(name = "get_promotion_rules_guide",
          description = """
                  프로모션 규칙 엔진 설계 가이드를 반환합니다.
                  if-else 한계, DB 기반 규칙 저장, Strategy/Specification 패턴, 우선순위 제어를 포함합니다.
                  '프로모션 설계', '프로모션 규칙', '할인 규칙 엔진', '프로모션 우선순위' 요청 시 호출하세요.
                  """)
    public String getPromotionRulesGuide() {
        return renderer.renderAll(searchService.getByCategory("coupon", "promotion"));
    }

    @Tool(name = "get_coupon_checklist",
          description = """
                  쿠폰/프로모션 구현 시 AI가 자주 빠뜨리는 패턴 체크리스트를 반환합니다.
                  유효성 검증, 할인 계산, 발급 동시성, 부분 취소 처리 항목을 포함합니다.
                  '쿠폰 체크리스트', '프로모션 체크리스트', '쿠폰 구현 확인' 요청 시 호출하세요.
                  """)
    public String getCouponChecklist() {
        return renderer.renderChecklist(searchService.getByCategory("coupon", "checklist"));
    }

    @Tool(name = "search_coupon_knowledge",
          description = """
                  키워드로 쿠폰/프로모션 도메인 지식을 검색합니다.
                  title, summary, tags, 섹션 내용에서 검색하며 관련도 순으로 반환합니다.
                  """)
    public String searchCouponKnowledge(
            @ToolParam(description = "검색 키워드 (예: 'redis', '선착순', '정률', '규칙 엔진')") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "검색 키워드를 입력해주세요.";
        }
        var results = searchService.search(KnowledgeQuery.ofDomainAndKeyword("coupon", keyword));
        return results.isEmpty()
                ? "관련 쿠폰/프로모션 도메인 지식을 찾을 수 없습니다. 키워드: " + keyword
                : renderer.renderSearchResults(results);
    }
}
