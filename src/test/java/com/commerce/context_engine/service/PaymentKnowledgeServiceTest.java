package com.commerce.context_engine.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentKnowledgeServiceTest {

    @Autowired
    PaymentKnowledgeService service;

    @Test
    void getWebhookGuide_containsKeyPrinciples() {
        String result = service.getWebhookGuide();
        assertThat(result).contains("HTTP 200");
        assertThat(result).contains("서명");
        assertThat(result).contains("멱등성");
    }

    @Test
    void getDuplicatePaymentGuard_containsThreeDefenses() {
        String result = service.getDuplicatePaymentGuard();
        assertThat(result).contains("주문 상태");
        assertThat(result).contains("멱등성 키");
        assertThat(result).contains("pg_payment_id");
    }

    @Test
    void getNetworkCancellationGuide_containsUncertainState() {
        String result = service.getNetworkCancellationGuide();
        assertThat(result).contains("UNCERTAIN");
        assertThat(result).contains("스케줄러");
    }

    @Test
    void getPartialRefundGuide_containsCouponCalculation() {
        String result = service.getPartialRefundGuide();
        assertThat(result).contains("쿠폰");
        assertThat(result).contains("포인트");
        assertThat(result).contains("정산");
    }

    @Test
    void getPaymentIdempotencyGuide_containsIdempotencyKey() {
        String result = service.getPaymentIdempotencyGuide();
        assertThat(result).contains("Idempotency-Key");
        assertThat(result).contains("재시도");
    }

    @Test
    void getChecklist_containsTenCheckboxes() {
        String result = service.getChecklist();
        long count = result.lines().filter(l -> l.contains("[ ]")).count();
        assertThat(count).isGreaterThanOrEqualTo(8);
    }

    @Test
    void search_byHmac_returnsWebhookItem() {
        String result = service.search("HMAC");
        assertThat(result).contains("웹훅");
    }

    @Test
    void search_unknownKeyword_returnsNotFoundMessage() {
        String result = service.search("존재하지않는키워드xyz");
        assertThat(result).contains("찾을 수 없습니다");
    }
}
