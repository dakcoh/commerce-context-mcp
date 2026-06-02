package com.commerce.context_engine.tool;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringCommerceContextToolTest {

    @Autowired
    SpringCommerceContextTool tool;

    @Test
    void getSpringCommerceImplementationContext_containsJavaSpringGuidance() {
        assertThat(tool.getSpringCommerceImplementationContext())
                .contains("Java Spring 구현 가이드")
                .contains("Spring Boot")
                .contains("JPA");
    }

    @Test
    void getSpringCommerceChecklist_containsCheckboxes() {
        assertThat(tool.getSpringCommerceChecklist()).contains("- [ ]");
    }

    @Test
    void searchSpringCommerceKnowledge_byOutbox_returnsMessagingGuide() {
        assertThat(tool.searchSpringCommerceKnowledge("Outbox"))
                .contains("멱등 Consumer")
                .contains("DLQ");
    }
}
