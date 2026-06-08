package com.commerce.context_engine.tool;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentContextToolTest {

    @Autowired
    PaymentContextTool tool;

    @Autowired
    ToolCallbackProvider toolCallbackProvider;

    @Test
    void paymentToolsRegistered() {
        List<String> names = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(cb -> cb.getToolDefinition().name())
                .toList();

        assertThat(names).contains(
                "get_payment_state_machine_guide",
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
    void getPaymentStateMachineGuide_containsAllStates() {
        String result = tool.getPaymentStateMachineGuide();
        assertThat(result).contains("PENDING");
        assertThat(result).contains("UNCERTAIN");
        assertThat(result).contains("PAID");
    }

    @Test
    void getPaymentWebhookGuide_returnsNonEmpty() {
        assertThat(tool.getPaymentWebhookGuide()).isNotBlank();
    }

    @Test
    void getDuplicatePaymentGuard_containsThreeDefenses() {
        String result = tool.getDuplicatePaymentGuard();
        assertThat(result).contains("3중 방어");
    }

    @Test
    void getNetworkCancellationGuide_containsScheduler() {
        String result = tool.getNetworkCancellationGuide();
        assertThat(result).contains("스케줄러");
    }

    @Test
    void getPartialRefundGuide_containsRefundCalculation() {
        String result = tool.getPartialRefundGuide();
        assertThat(result).contains("역산");
    }

    @Test
    void getPaymentIdempotencyGuide_containsPgHeader() {
        String result = tool.getPaymentIdempotencyGuide();
        assertThat(result).contains("Idempotency-Key");
    }

    @Test
    void getPaymentChecklist_containsCheckboxes() {
        String result = tool.getPaymentChecklist();
        assertThat(result).contains("[ ]");
    }

    @Test
    void searchPaymentKnowledge_byWebhook_returnsWebhookItem() {
        String result = tool.searchPaymentKnowledge("웹훅");
        assertThat(result).contains("HTTP 200");
    }
}
