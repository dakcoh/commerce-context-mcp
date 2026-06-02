package com.commerce.context_engine.tool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommerceContextToolTest {

    @Autowired
    CommerceContextTool tool;

    @Test
    void getCommerceFoundationContext_containsGeneralDistributionKnowledge() {
        assertThat(tool.getCommerceFoundationContext())
                .contains("상품 카탈로그")
                .contains("배송과 반품")
                .contains("유통 채널과 판매자")
                .contains("운영 정합성");
    }

    @Test
    void getCommerceFoundationChecklist_containsCheckboxes() {
        assertThat(tool.getCommerceFoundationChecklist()).contains("- [ ]");
    }

    @Test
    void searchCommerceKnowledge_byPartialCancel_returnsRelevantKnowledge() {
        assertThat(tool.searchCommerceKnowledge("부분 취소"))
                .contains("commerce-order-lifecycle")
                .contains("commerce-promotion-allocation");
    }
}
