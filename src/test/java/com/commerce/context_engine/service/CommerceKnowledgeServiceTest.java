package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.commerce.CommerceKnowledgeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CommerceKnowledgeServiceTest {

    @Autowired
    CommerceKnowledgeService service;

    @Autowired
    CommerceKnowledgeProperties properties;

    @Test
    void containsTwentyNormalizedKnowledgeItems() {
        assertThat(properties.getItems()).hasSize(20);
        assertThat(properties.getItems()).allSatisfy(item -> {
            assertThat(item.getId()).isNotBlank();
            assertThat(item.getCategory()).isNotBlank();
            assertThat(item.getTitle()).isNotBlank();
            assertThat(item.getSummary()).isNotBlank();
            assertThat(item.getBusinessContext()).isNotBlank();
            assertThat(item.getInvariants()).isNotEmpty();
            assertThat(item.getWorkflow()).isNotEmpty();
            assertThat(item.getTechnicalGuidance()).isNotEmpty();
            assertThat(item.getFailureScenarios()).isNotEmpty();
            assertThat(item.getChecklist()).isNotEmpty();
            assertThat(item.getTags()).isNotEmpty();
        });
    }

    @Test
    void getFoundationContext_containsTraceableKnowledgeMetadata() {
        assertThat(service.getFoundationContext())
                .contains("knowledgeId")
                .contains("commerce-catalog-model")
                .contains("commerce-operational-integrity")
                .contains("commerce-security-privacy")
                .contains("commerce-loyalty-point-ledger")
                .contains("commerce-ops-slo-incident");
    }

    @Test
    void getChecklist_containsAllKnowledgeIdsAndCheckboxes() {
        assertThat(service.getChecklist())
                .contains("commerce-catalog-model")
                .contains("commerce-settlement-reconciliation")
                .contains("commerce-cart-checkout")
                .contains("commerce-subscription-recurring-order")
                .contains("- [ ]");
    }

    @Test
    void search_bySku_returnsCatalogKnowledge() {
        assertThat(service.search("sku"))
                .contains("Product, SKU, Offer")
                .contains("commerce-catalog-model");
    }

    @Test
    void search_bySeller_returnsDistributionAndSettlementKnowledge() {
        assertThat(service.search("판매자"))
                .contains("commerce-channel-seller-distribution")
                .contains("commerce-settlement-reconciliation");
    }

    @Test
    void search_byOutbox_returnsOperationalKnowledge() {
        assertThat(service.search("outbox"))
                .contains("commerce-operational-integrity");
    }

    @Test
    void search_byPoint_returnsLoyaltyKnowledge() {
        assertThat(service.search("포인트"))
                .contains("commerce-loyalty-point-ledger")
                .contains("원장");
    }

    @Test
    void search_unknownKeyword_returnsNotFoundMessage() {
        assertThat(service.search("존재하지않는xyz")).contains("찾을 수 없습니다");
    }

    @Test
    void search_blankKeyword_returnsKeywordRequiredMessage() {
        assertThat(service.search("   ")).contains("search keyword");
    }

    @Test
    void search_nullKeyword_returnsKeywordRequiredMessage() {
        assertThat(service.search(null)).contains("search keyword");
    }
}
