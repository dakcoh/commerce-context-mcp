package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.payment.PaymentKnowledgeProperties;
import com.commerce.context_engine.domain.payment.PaymentKnowledgeProperties.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.commerce.context_engine.service.KnowledgeSearchSupport.containsNormalized;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.hasSearchKeyword;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.missingKeywordMessage;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.normalize;
import static com.commerce.context_engine.service.KnowledgeSearchSupport.safeList;

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
        if (!hasSearchKeyword(keyword)) {
            return missingKeywordMessage();
        }

        String lower = normalize(keyword);
        List<Item> matched = safeList(properties.getItems()).stream()
                .filter(item ->
                        containsNormalized(item.getTitle(), lower)
                        || containsNormalized(item.getContent(), lower)
                        || safeList(item.getTags()).stream().anyMatch(t -> containsNormalized(t, lower)))
                .collect(Collectors.toList());

        if (matched.isEmpty()) {
            return "관련 결제 도메인 지식을 찾을 수 없습니다. 키워드: " + keyword;
        }
        return format(matched);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<Item> filterByCategory(String category) {
        return safeList(properties.getItems()).stream()
                .filter(i -> category.equals(i.getCategory()))
                .collect(Collectors.toList());
    }

    private String format(List<Item> items) {
        return safeList(items).stream()
                .map(i -> "## " + i.getTitle() + "\n" + normalizeContent(i.getContent()))
                .collect(Collectors.joining("\n\n"));
    }

    private String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }
}
