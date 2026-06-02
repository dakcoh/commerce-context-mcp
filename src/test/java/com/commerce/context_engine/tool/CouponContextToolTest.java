package com.commerce.context_engine.tool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponContextToolTest {

    @Autowired
    CouponContextTool tool;

    @Test
    void getCouponValidationGuide_containsConcurrencyGuard() {
        assertThat(tool.getCouponValidationGuide()).contains("동시성");
    }

    @Test
    void getCouponDiscountCalculation_containsCap() {
        assertThat(tool.getCouponDiscountCalculation()).contains("cap");
    }

    @Test
    void getCouponIssuanceGuide_containsRedis() {
        assertThat(tool.getCouponIssuanceGuide()).contains("Redis");
    }

    @Test
    void getPromotionRulesGuide_containsStrategyPattern() {
        assertThat(tool.getPromotionRulesGuide()).contains("Strategy");
    }

    @Test
    void getCouponChecklist_containsCheckboxes() {
        assertThat(tool.getCouponChecklist()).contains("[ ]");
    }

    @Test
    void searchCouponKnowledge_byRedis_returnsIssuanceGuide() {
        assertThat(tool.searchCouponKnowledge("redis")).contains("선착순");
    }
}
