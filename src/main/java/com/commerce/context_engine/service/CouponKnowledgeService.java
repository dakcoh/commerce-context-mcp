package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.coupon.CouponKnowledgeProperties;
import com.commerce.context_engine.domain.coupon.CouponKnowledgeProperties.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponKnowledgeService {

    private final CouponKnowledgeProperties properties;

    /** 쿠폰 유효성 검증 가이드 */
    public String getCouponValidation() {
        return format(filterByCategory("validation"));
    }

    /** 쿠폰 할인 금액 계산 가이드 */
    public String getCouponDiscountCalculation() {
        return format(filterByCategory("calculation"));
    }

    /** 선착순 쿠폰 발급 동시성 제어 */
    public String getCouponIssuanceGuide() {
        return format(filterByCategory("issuance"));
    }

    /** 프로모션 규칙 엔진 설계 */
    public String getPromotionRules() {
        return format(filterByCategory("promotion"));
    }

    /** AI가 자주 빠뜨리는 쿠폰/프로모션 패턴 체크리스트 */
    public String getChecklist() {
        return filterByCategory("checklist").stream()
                .map(Item::getContent)
                .collect(Collectors.joining("\n"));
    }

    /** 키워드로 쿠폰/프로모션 지식 검색 */
    public String search(String keyword) {
        String lower = keyword.toLowerCase();
        List<Item> matched = properties.getItems().stream()
                .filter(item ->
                        item.getTitle().toLowerCase().contains(lower)
                        || item.getContent().toLowerCase().contains(lower)
                        || item.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lower)))
                .collect(Collectors.toList());

        if (matched.isEmpty()) {
            return "관련 쿠폰/프로모션 도메인 지식을 찾을 수 없습니다. 키워드: " + keyword;
        }
        return format(matched);
    }

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
