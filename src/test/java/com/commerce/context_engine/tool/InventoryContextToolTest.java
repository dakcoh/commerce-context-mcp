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
    void paymentToolsRegistered() {
        List<String> names = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(cb -> cb.getToolDefinition().name())
                .toList();

        assertThat(names).contains(
                "get_payment_webhook_guide",
                "get_duplicate_payment_guard",
                "get_network_cancellation_guide",
                "get_partial_refund_guide",
                "get_payment_idempotency_guide",
                "get_payment_checklist",
                "search_payment_knowledge"
        );
    }

    @Test
    void totalToolCountAtLeast13() {
        // 도메인 추가 시 증가 — 최소 재고(6) + 결제(7) = 13개 이상
        assertThat(toolCallbackProvider.getToolCallbacks().length).isGreaterThanOrEqualTo(13);
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
