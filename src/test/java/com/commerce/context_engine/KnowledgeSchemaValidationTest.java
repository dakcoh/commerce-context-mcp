package com.commerce.context_engine;

import com.commerce.context_engine.domain.SimpleKnowledgeItem;
import com.commerce.context_engine.domain.commerce.CommerceKnowledgeProperties;
import com.commerce.context_engine.domain.coupon.CouponKnowledgeProperties;
import com.commerce.context_engine.domain.inventory.InventoryKnowledgeProperties;
import com.commerce.context_engine.domain.payment.PaymentKnowledgeProperties;
import com.commerce.context_engine.domain.settlement.SettlementKnowledgeProperties;
import com.commerce.context_engine.domain.springcommerce.SpringCommerceKnowledgeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class KnowledgeSchemaValidationTest {

    private static final Pattern KNOWLEDGE_ID = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");

    private static final Set<String> INVENTORY_CATEGORIES = Set.of(
            "lifecycle", "concurrency", "idempotency", "consistency", "checklist");
    private static final Set<String> PAYMENT_CATEGORIES = Set.of(
            "webhook", "duplicate", "network-cancel", "refund", "idempotency", "state-machine", "checklist");
    private static final Set<String> SETTLEMENT_CATEGORIES = Set.of(
            "timing", "deduction", "cycle", "integrity", "checklist");
    private static final Set<String> COUPON_CATEGORIES = Set.of(
            "validation", "calculation", "issuance", "promotion", "checklist");
    private static final Set<String> COMMERCE_CATEGORIES = Set.of(
            "catalog", "pricing", "order", "inventory", "payment", "fulfillment",
            "promotion", "distribution", "settlement", "operations", "customer",
            "checkout", "search", "claim", "security", "loyalty", "membership",
            "review", "subscription");
    private static final Set<String> SPRING_COMMERCE_CATEGORIES = Set.of(
            "architecture", "transaction", "persistence", "concurrency", "api",
            "messaging", "cache", "batch", "quality", "java-core", "security",
            "configuration", "integration", "operations");

    @Autowired
    InventoryKnowledgeProperties inventory;

    @Autowired
    PaymentKnowledgeProperties payment;

    @Autowired
    SettlementKnowledgeProperties settlement;

    @Autowired
    CouponKnowledgeProperties coupon;

    @Autowired
    CommerceKnowledgeProperties commerce;

    @Autowired
    SpringCommerceKnowledgeProperties springCommerce;

    @Test
    void allKnowledgeItems_haveGloballyUniqueValidIds() {
        List<String> ids = Stream.of(
                        inventory.getItems().stream().map(SimpleKnowledgeItem::getId),
                        payment.getItems().stream().map(SimpleKnowledgeItem::getId),
                        settlement.getItems().stream().map(SimpleKnowledgeItem::getId),
                        coupon.getItems().stream().map(SimpleKnowledgeItem::getId),
                        commerce.getItems().stream().map(CommerceKnowledgeProperties.Item::getId),
                        springCommerce.getItems().stream().map(SpringCommerceKnowledgeProperties.Item::getId)
                )
                .flatMap(s -> s)
                .toList();

        assertThat(ids)
                .isNotEmpty()
                .allMatch(id -> KNOWLEDGE_ID.matcher(id).matches())
                .doesNotHaveDuplicates();
    }

    @Test
    void simpleDomainKnowledge_hasRequiredFieldsAndAllowedCategories() {
        assertSimpleItems("inventory", inventory.getItems(), INVENTORY_CATEGORIES);
        assertSimpleItems("payment", payment.getItems(), PAYMENT_CATEGORIES);
        assertSimpleItems("settlement", settlement.getItems(), SETTLEMENT_CATEGORIES);
        assertSimpleItems("coupon", coupon.getItems(), COUPON_CATEGORIES);
    }

    @Test
    void commerceKnowledge_hasNormalizedSchemaAndAllowedCategories() {
        assertThat(commerce.getItems()).isNotEmpty();

        assertThat(commerce.getItems()).allSatisfy(item -> {
            assertCommonFields("commerce", item.getId(), item.getCategory(), item.getTitle(), item.getTags(), COMMERCE_CATEGORIES);
            assertThat(item.getSummary()).as(item.getId() + " summary").isNotBlank();
            assertThat(item.getBusinessContext()).as(item.getId() + " businessContext").isNotBlank();
            assertRequiredList(item.getId(), "invariants", item.getInvariants());
            assertRequiredList(item.getId(), "workflow", item.getWorkflow());
            assertRequiredList(item.getId(), "technicalGuidance", item.getTechnicalGuidance());
            assertRequiredList(item.getId(), "failureScenarios", item.getFailureScenarios());
            assertRequiredList(item.getId(), "checklist", item.getChecklist());
        });
    }

    @Test
    void springCommerceKnowledge_hasNormalizedSchemaAndAllowedCategories() {
        assertThat(springCommerce.getItems()).isNotEmpty();

        assertThat(springCommerce.getItems()).allSatisfy(item -> {
            assertCommonFields("spring-commerce", item.getId(), item.getCategory(), item.getTitle(), item.getTags(), SPRING_COMMERCE_CATEGORIES);
            assertThat(item.getSummary()).as(item.getId() + " summary").isNotBlank();
            assertThat(item.getBusinessContext()).as(item.getId() + " businessContext").isNotBlank();
            assertRequiredList(item.getId(), "springGuidance", item.getSpringGuidance());
            assertRequiredList(item.getId(), "avoidPatterns", item.getAvoidPatterns());
            assertRequiredList(item.getId(), "checklist", item.getChecklist());
        });
    }

    private static void assertSimpleItems(String domain, List<SimpleKnowledgeItem> items, Set<String> allowedCategories) {
        assertThat(items).as(domain + " items").isNotEmpty();

        assertThat(items).allSatisfy(item -> {
            assertCommonFields(domain, item.getId(), item.getCategory(), item.getTitle(), item.getTags(), allowedCategories);
            assertThat(item.getSummary()).as(item.getId() + " summary").isNotBlank();
            // checklist 카테고리는 checklist 필드만 필수
            if ("checklist".equals(item.getCategory())) {
                assertRequiredList(item.getId(), "checklist", item.getChecklist());
            } else {
                assertRequiredList(item.getId(), "guidance", item.getGuidance());
                assertRequiredList(item.getId(), "avoidPatterns", item.getAvoidPatterns());
            }
        });
    }

    private static void assertCommonFields(
            String domain,
            String id,
            String category,
            String title,
            List<String> tags,
            Set<String> allowedCategories
    ) {
        assertThat(id).as(domain + " id").isNotBlank();
        assertThat(category).as(id + " category").isIn(allowedCategories);
        assertThat(title).as(id + " title").isNotBlank();
        assertRequiredList(id, "tags", tags);
        assertThat(tags).as(id + " tags").doesNotHaveDuplicates();
    }

    private static void assertRequiredList(String id, String fieldName, List<String> values) {
        assertThat(values).as(id + " " + fieldName).isNotEmpty();
        assertThat(values).as(id + " " + fieldName).allSatisfy(value -> assertThat(value).isNotBlank());
    }

}
