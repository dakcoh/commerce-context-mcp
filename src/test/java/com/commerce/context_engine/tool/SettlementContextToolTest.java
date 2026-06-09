package com.commerce.context_engine.tool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SettlementContextToolTest {

    @Autowired
    SettlementContextTool tool;

    @Test
    void getSettlementTiming_containsBusinessDayHandling() {
        assertThat(tool.getSettlementTiming()).contains("영업일");
    }

    @Test
    void getSettlementDeduction_containsNegativeSettlement() {
        assertThat(tool.getSettlementDeduction()).contains("음수");
    }

    @Test
    void getSettlementCycle_containsIdempotency() {
        assertThat(tool.getSettlementCycle()).contains("멱등성");
    }

    @Test
    void getSettlementIntegrity_containsTimezone() {
        assertThat(tool.getSettlementIntegrity()).contains("타임존");
    }

    @Test
    void getSettlementStatement_containsSnapshot() {
        assertThat(tool.getSettlementStatement()).contains("스냅샷");
    }

    @Test
    void getSettlementTax_containsVat() {
        assertThat(tool.getSettlementTax()).contains("부가세");
    }

    @Test
    void getSettlementHold_containsDispute() {
        assertThat(tool.getSettlementHold()).contains("분쟁");
    }

    @Test
    void getSettlementPayout_containsIdempotentTransfer() {
        String result = tool.getSettlementPayout();
        assertThat(result).contains("이체");
        assertThat(result).contains("멱등");
    }

    @Test
    void getSettlementChecklist_containsCheckboxes() {
        assertThat(tool.getSettlementChecklist()).contains("[ ]");
    }

    @Test
    void searchSettlementKnowledge_byBatch_returnsCycleGuide() {
        assertThat(tool.searchSettlementKnowledge("배치")).contains("페이징");
    }
}
