package com.commerce.context_engine;

import com.commerce.context_engine.domain.commerce.CommerceKnowledgeProperties;
import com.commerce.context_engine.domain.coupon.CouponKnowledgeProperties;
import com.commerce.context_engine.domain.inventory.InventoryKnowledgeProperties;
import com.commerce.context_engine.domain.payment.PaymentKnowledgeProperties;
import com.commerce.context_engine.domain.settlement.SettlementKnowledgeProperties;
import com.commerce.context_engine.domain.springcommerce.SpringCommerceKnowledgeProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collection;
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
            "webhook", "duplicate", "network-cancel", "refund", "idempotency", "checklist");
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
                        simpleItems("inventory", inventoryItems()),
                        simpleItems("payment", paymentItems()),
                        simpleItems("settlement", settlementItems()),
                        simpleItems("coupon", couponItems()),
                        commerceItems(),
                        springCommerceItems()
                )
                .flatMap(Collection::stream)
                .map(KnowledgeItem::id)
                .toList();

        assertThat(ids)
                .isNotEmpty()
                .allMatch(id -> KNOWLEDGE_ID.matcher(id).matches())
                .doesNotHaveDuplicates();
    }

    @Test
    void simpleDomainKnowledge_hasRequiredFieldsAndAllowedCategories() {
        assertSimpleItems("inventory", inventoryItems(), INVENTORY_CATEGORIES);
        assertSimpleItems("payment", paymentItems(), PAYMENT_CATEGORIES);
        assertSimpleItems("settlement", settlementItems(), SETTLEMENT_CATEGORIES);
        assertSimpleItems("coupon", couponItems(), COUPON_CATEGORIES);
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
            assertCommonFields(domain, item.id(), item.category(), item.title(), item.tags(), allowedCategories);
            assertThat(item.content()).as(item.id() + " content").isNotBlank();
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

    private List<KnowledgeItem> commerceItems() {
        return commerce.getItems().stream()
                .map(item -> new KnowledgeItem("commerce", item.getId()))
                .toList();
    }

    private List<KnowledgeItem> springCommerceItems() {
        return springCommerce.getItems().stream()
                .map(item -> new KnowledgeItem("spring-commerce", item.getId()))
                .toList();
    }

    private static List<KnowledgeItem> simpleItems(String domain, List<? extends SimpleKnowledgeItem> items) {
        return items.stream()
                .map(item -> new KnowledgeItem(domain, item.id()))
                .toList();
    }

    private List<SimpleKnowledgeItem> inventoryItems() {
        return inventory.getItems().stream()
                .map(item -> new SimpleKnowledgeItem(item.getId(), item.getCategory(), item.getTitle(), item.getContent(), item.getTags()))
                .toList();
    }

    private List<SimpleKnowledgeItem> paymentItems() {
        return payment.getItems().stream()
                .map(item -> new SimpleKnowledgeItem(item.getId(), item.getCategory(), item.getTitle(), item.getContent(), item.getTags()))
                .toList();
    }

    private List<SimpleKnowledgeItem> settlementItems() {
        return settlement.getItems().stream()
                .map(item -> new SimpleKnowledgeItem(item.getId(), item.getCategory(), item.getTitle(), item.getContent(), item.getTags()))
                .toList();
    }

    private List<SimpleKnowledgeItem> couponItems() {
        return coupon.getItems().stream()
                .map(item -> new SimpleKnowledgeItem(item.getId(), item.getCategory(), item.getTitle(), item.getContent(), item.getTags()))
                .toList();
    }

    private record KnowledgeItem(String domain, String id) {
    }

    private record SimpleKnowledgeItem(
            String id,
            String category,
            String title,
            String content,
            List<String> tags
    ) {
    }
}
