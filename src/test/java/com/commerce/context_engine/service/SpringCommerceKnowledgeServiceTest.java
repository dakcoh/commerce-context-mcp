package com.commerce.context_engine.service;

import com.commerce.context_engine.domain.springcommerce.SpringCommerceKnowledgeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringCommerceKnowledgeServiceTest {

    @Autowired
    SpringCommerceKnowledgeService service;

    @Autowired
    SpringCommerceKnowledgeProperties properties;

    @Test
    void containsNineNormalizedSpringKnowledgeItems() {
        assertThat(properties.getItems()).hasSize(9);
        assertThat(properties.getItems()).allSatisfy(item -> {
            assertThat(item.getId()).isNotBlank();
            assertThat(item.getCategory()).isNotBlank();
            assertThat(item.getTitle()).isNotBlank();
            assertThat(item.getSummary()).isNotBlank();
            assertThat(item.getBusinessContext()).isNotBlank();
            assertThat(item.getSpringGuidance()).isNotEmpty();
            assertThat(item.getAvoidPatterns()).isNotEmpty();
            assertThat(item.getChecklist()).isNotEmpty();
            assertThat(item.getTags()).isNotEmpty();
        });
    }

    @Test
    void getSpringImplementationContext_containsCoreSpringTopics() {
        assertThat(service.getSpringImplementationContext())
                .contains("모듈형 모놀리스")
                .contains("@Transactional")
                .contains("JPA")
                .contains("Outbox")
                .contains("Testcontainers");
    }

    @Test
    void getChecklist_containsKnowledgeIdsAndCheckboxes() {
        assertThat(service.getChecklist())
                .contains("spring-commerce-transaction-boundary")
                .contains("spring-commerce-testing-observability")
                .contains("- [ ]");
    }

    @Test
    void search_byTransactional_returnsBoundaryGuide() {
        assertThat(service.search("@Transactional"))
                .contains("트랜잭션 경계")
                .contains("self-invocation");
    }

    @Test
    void search_byRedis_returnsCacheAndConcurrencyGuidance() {
        assertThat(service.search("Redis"))
                .contains("원장과 가속 계층")
                .contains("DB UNIQUE 제약");
    }

    @Test
    void search_byTestcontainers_returnsQualityGuide() {
        assertThat(service.search("Testcontainers"))
                .contains("PostgreSQL과 Redis")
                .contains("동시 요청 테스트");
    }

    @Test
    void search_unknownKeyword_returnsNotFoundMessage() {
        assertThat(service.search("존재하지않는xyz")).contains("찾을 수 없습니다");
    }
}
