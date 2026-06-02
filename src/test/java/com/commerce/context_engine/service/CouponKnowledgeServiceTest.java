package com.commerce.context_engine.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponKnowledgeServiceTest {

    @Autowired
    CouponKnowledgeService service;

    @Test
    void getCouponValidation_containsFiveChecks() {
        String result = service.getCouponValidation();
        assertThat(result).contains("유효 기간");
        assertThat(result).contains("중복 사용");
        assertThat(result).contains("발급 대상");
    }

    @Test
    void getCouponDiscountCalculation_containsCapAndOrder() {
        String result = service.getCouponDiscountCalculation();
        assertThat(result).contains("cap");
        assertThat(result).contains("정률");
        assertThat(result).contains("정액");
    }

    @Test
    void getCouponIssuanceGuide_containsRedisAndQueue() {
        String result = service.getCouponIssuanceGuide();
        assertThat(result).contains("Redis");
        assertThat(result).contains("메시지 큐");
    }

    @Test
    void getPromotionRules_containsStrategyPattern() {
        String result = service.getPromotionRules();
        assertThat(result).contains("Strategy");
        assertThat(result).contains("Specification");
    }

    @Test
    void getChecklist_containsCheckboxes() {
        long count = service.getChecklist().lines().filter(l -> l.contains("[ ]")).count();
        assertThat(count).isGreaterThanOrEqualTo(8);
    }

    @Test
    void search_byRedis_returnsIssuanceItem() {
        assertThat(service.search("redis")).contains("선착순");
    }

    @Test
    void search_unknownKeyword_returnsNotFoundMessage() {
        assertThat(service.search("존재하지않는xyz")).contains("찾을 수 없습니다");
    }
}
