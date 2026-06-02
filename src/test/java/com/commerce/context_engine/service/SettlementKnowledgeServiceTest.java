package com.commerce.context_engine.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SettlementKnowledgeServiceTest {

    @Autowired
    SettlementKnowledgeService service;

    @Test
    void getSettlementTiming_containsThreeTypes() {
        String result = service.getSettlementTiming();
        assertThat(result).contains("구매 확정");
        assertThat(result).contains("배송 완료");
        assertThat(result).contains("결제 완료");
    }

    @Test
    void getSettlementDeduction_containsFormulaAndNegative() {
        String result = service.getSettlementDeduction();
        assertThat(result).contains("수수료");
        assertThat(result).contains("음수");
    }

    @Test
    void getSettlementCycle_containsIdempotency() {
        String result = service.getSettlementCycle();
        assertThat(result).contains("멱등성");
        assertThat(result).contains("페이징");
    }

    @Test
    void getSettlementIntegrity_containsTimezone() {
        String result = service.getSettlementIntegrity();
        assertThat(result).contains("타임존");
        assertThat(result).contains("정합성");
    }

    @Test
    void getChecklist_containsCheckboxes() {
        long count = service.getChecklist().lines().filter(l -> l.contains("[ ]")).count();
        assertThat(count).isGreaterThanOrEqualTo(8);
    }

    @Test
    void search_byBatch_returnsSettlementCycle() {
        assertThat(service.search("배치")).contains("멱등성");
    }

    @Test
    void search_unknownKeyword_returnsNotFoundMessage() {
        assertThat(service.search("존재하지않는xyz")).contains("찾을 수 없습니다");
    }
}
