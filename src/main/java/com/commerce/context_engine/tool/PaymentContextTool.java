package com.commerce.context_engine.tool;

import com.commerce.context_engine.service.PaymentKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentContextTool {

    private final PaymentKnowledgeService knowledgeService;

    @Tool(name = "get_payment_webhook_guide",
          description = """
                  결제 PG사 웹훅 처리 가이드를 반환합니다.
                  수신 즉시 200 응답, 비동기 처리, HMAC-SHA256 서명 검증, 중복 수신 방어 방법을 포함합니다.
                  '웹훅', 'webhook', '결제 알림', 'PG 콜백', '결제 완료 처리' 요청 시 호출하세요.
                  """)
    public String getPaymentWebhookGuide() {
        return knowledgeService.getWebhookGuide();
    }

    @Tool(name = "get_duplicate_payment_guard",
          description = """
                  중복 결제 방어 전략을 반환합니다.
                  주문 상태 검증, 멱등성 키, PG사 결제 번호 중복 확인 3중 방어 방법을 포함합니다.
                  '중복 결제', '이중 결제', '결제 중복', 'double payment' 요청 시 호출하세요.
                  """)
    public String getDuplicatePaymentGuard() {
        return knowledgeService.getDuplicatePaymentGuard();
    }

    @Tool(name = "get_network_cancellation_guide",
          description = """
                  망취소 처리 가이드를 반환합니다.
                  결제 응답을 받지 못한 상태(타임아웃, 네트워크 단절)에서 결제 상태를 확정하는 방법을 포함합니다.
                  '망취소', '타임아웃', '결제 응답 없음', '결제 상태 불명확', 'network cancel' 요청 시 호출하세요.
                  """)
    public String getNetworkCancellationGuide() {
        return knowledgeService.getNetworkCancellationGuide();
    }

    @Tool(name = "get_partial_refund_guide",
          description = """
                  부분 취소 및 환불 처리 가이드를 반환합니다.
                  쿠폰/포인트 병행 사용 시 환불 금액 역산, 정산 완료 건 환불 처리를 포함합니다.
                  '부분 취소', '부분 환불', '환불 금액 계산', '쿠폰 환불', '포인트 환불' 요청 시 호출하세요.
                  """)
    public String getPartialRefundGuide() {
        return knowledgeService.getPartialRefundGuide();
    }

    @Tool(name = "get_payment_idempotency_guide",
          description = """
                  결제 API 멱등성 키 설계 가이드를 반환합니다.
                  이중 결제 방지를 위한 키 생성 전략, PG사 Idempotency-Key 헤더 사용법을 포함합니다.
                  '결제 멱등성', 'Idempotency-Key', '결제 재시도', '결제 중복 방지' 요청 시 호출하세요.
                  """)
    public String getPaymentIdempotencyGuide() {
        return knowledgeService.getPaymentIdempotencyGuide();
    }

    @Tool(name = "get_payment_checklist",
          description = """
                  결제 구현 시 AI가 자주 빠뜨리는 패턴 체크리스트를 반환합니다.
                  웹훅, 중복 결제, 망취소, 환불 처리 전반을 망라한 검토 목록입니다.
                  '결제 체크리스트', '결제 검토', '결제 구현 확인' 요청 시 호출하세요.
                  """)
    public String getPaymentChecklist() {
        return knowledgeService.getChecklist();
    }

    @Tool(name = "search_payment_knowledge",
          description = """
                  키워드로 결제 도메인 지식을 검색합니다.
                  title, content, tags에서 검색하며 관련 항목을 반환합니다.
                  특정 결제 주제에 대한 지식을 찾을 때 사용하세요.
                  """)
    public String searchPaymentKnowledge(
            @ToolParam(description = "검색 키워드 (예: 'HMAC', '망취소', '부분환불', '멱등성')") String keyword) {
        return knowledgeService.search(keyword);
    }
}
