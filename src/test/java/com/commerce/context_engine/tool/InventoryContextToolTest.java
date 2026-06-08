package com.commerce.context_engine.tool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InventoryContextToolTest {

    @Autowired
    InventoryContextTool tool;

    @Autowired
    ToolCallbackProvider toolCallbackProvider;

    @Test
    void inventoryToolsRegistered() {
        List<String> names = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(cb -> cb.getToolDefinition().name())
                .toList();

        assertThat(names).contains(
                "get_inventory_context",
                "get_concurrency_strategy",
                "get_saga_pattern",
                "get_idempotency_guide",
                "get_inventory_checklist",
                "search_inventory_knowledge"
        );
    }

    @Test
    void allToolsRegistered() {
        List<String> names = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(cb -> cb.getToolDefinition().name())
                .toList();

        assertThat(names).contains(
                "get_payment_state_machine_guide",
                "get_settlement_timing",
                "get_settlement_deduction",
                "get_settlement_cycle",
                "get_settlement_integrity",
                "get_settlement_checklist",
                "search_settlement_knowledge",
                "get_coupon_validation_guide",
                "get_coupon_discount_calculation",
                "get_coupon_issuance_guide",
                "get_promotion_rules_guide",
                "get_coupon_checklist",
                "search_coupon_knowledge",
                "get_commerce_foundation_context",
                "get_commerce_foundation_checklist",
                "search_commerce_knowledge",
                "get_spring_commerce_implementation_context",
                "get_spring_commerce_checklist",
                "search_spring_commerce_knowledge"
        );
        assertThat(names).hasSize(33);
    }

    @Test
    void getInventoryContext_returnsNonEmptyString() {
        assertThat(tool.getInventoryContext()).isNotBlank();
    }

    @Test
    void getConcurrencyStrategy_containsThreeLockStrategies() {
        String result = tool.getConcurrencyStrategy();
        assertThat(result).contains("낙관락");
        assertThat(result).contains("비관락");
        assertThat(result).contains("분산락");
    }

    @Test
    void getSagaPattern_containsCompensationFlow() {
        String result = tool.getSagaPattern();
        assertThat(result).contains("보상");
        assertThat(result).contains("Outbox");
    }

    @Test
    void getIdempotencyGuide_containsIdempotencyKey() {
        assertThat(tool.getIdempotencyGuide()).contains("idempotency key");
    }

    @Test
    void getInventoryChecklist_containsCheckboxes() {
        assertThat(tool.getInventoryChecklist()).contains("[ ]");
    }

    @Test
    void searchInventoryKnowledge_byRedis_returnsDistributedLock() {
        assertThat(tool.searchInventoryKnowledge("redis")).contains("분산락");
    }
}
