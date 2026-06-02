package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.payment.PaymentKnowledgeProperties;
import com.commerce.context_engine.domain.payment.PaymentKnowledgeProperties.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentKnowledgeService {

    private final PaymentKnowledgeProperties properties;

    /** 결제 웹훅 처리 가이드 */
    public String getWebhookGuide() {
        return format(filterByCategory("webhook"));
    }

    /** 중복 결제 방어 전략 */
    public String getDuplicatePaymentGuard() {
        return format(filterByCategory("duplicate"));
    }

    /** 망취소 처리 (결제 응답 불명확 상황) */
    public String getNetworkCancellationGuide() {
        return format(filterByCategory("network-cancel"));
    }

    /** 부분 취소 / 환불 처리 */
    public String getPartialRefundGuide() {
        return format(filterByCategory("refund"));
    }

    /** 결제 API 멱등성 키 설계 */
    public String getPaymentIdempotencyGuide() {
        return format(filterByCategory("idempotency"));
    }

    /** AI가 자주 빠뜨리는 결제 패턴 체크리스트 */
    public String getChecklist() {
        return filterByCategory("checklist").stream()
                .map(Item::getContent)
                .collect(Collectors.joining("\n"));
    }

    /**
     * 키워드로 결제 지식 검색.
     * title, content, tags 중 하나라도 keyword를 포함하면 반환.
     */
    public String search(String keyword) {
        String lower = keyword.toLowerCase();
        List<Item> matched = properties.getItems().stream()
                .filter(item ->
                        item.getTitle().toLowerCase().contains(lower)
                        || item.getContent().toLowerCase().contains(lower)
                        || item.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lower)))
                .collect(Collectors.toList());

        if (matched.isEmpty()) {
            return "관련 결제 도메인 지식을 찾을 수 없습니다. 키워드: " + keyword;
        }
        return format(matched);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<Item> filterByCategory(String category) {
        return properties.getItems().stream()
                .filter(i -> category.equals(i.getCategory()))
                .collect(Collectors.toList());
    }

    private String format(List<Item> items) {
        return items.stream()
                .map(i -> "## " + i.getTitle() + "\n" + i.getContent().trim())
                .collect(Collectors.joining("\n\n"));
    }
}
