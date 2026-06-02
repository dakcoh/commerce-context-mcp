package com.commerce.context_engine.tool;

import com.commerce.context_engine.service.CommerceKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommerceContextTool {

    private final CommerceKnowledgeService knowledgeService;

    @Tool(name = "get_commerce_foundation_context",
          description = """
                  범용 이커머스 설계 지식 전체를 반환합니다.
                  상품 카탈로그, 가격, 주문, 재고, 결제, 배송·반품, 프로모션, 유통 채널, 정산, 운영 정합성을 포함합니다.
                  쇼핑몰 신규 구축, 전체 아키텍처 설계, 이커머스 기능 우선순위 검토 요청 시 호출하세요.
                  """)
    public String getCommerceFoundationContext() {
        return knowledgeService.getFoundationContext();
    }

    @Tool(name = "get_commerce_foundation_checklist",
          description = """
                  범용 이커머스 설계 검토 체크리스트를 반환합니다.
                  신규 쇼핑몰 구현 범위 점검, 아키텍처 리뷰, 출시 전 누락 확인 요청 시 호출하세요.
                  """)
    public String getCommerceFoundationChecklist() {
        return knowledgeService.getChecklist();
    }

    @Tool(name = "search_commerce_knowledge",
          description = """
                  키워드로 범용 이커머스 지식을 검색합니다.
                  재고, 결제, 상품, SKU, 배송, 반품, 채널, 셀러, 정산, 운영 정합성 등 여러 도메인을 함께 탐색할 때 사용하세요.
                  """)
    public String searchCommerceKnowledge(
            @ToolParam(description = "검색 키워드 (예: 'sku', '멀티 창고', '부분 취소', 'outbox', '판매자')") String keyword) {
        return knowledgeService.search(keyword);
    }
}
