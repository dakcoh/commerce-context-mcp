package com.commerce.context_engine.repository;

import com.commerce.context_engine.core.KnowledgeEntry;
import com.commerce.context_engine.core.KnowledgeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class YamlKnowledgeRepositoryTest {

    @Autowired
    KnowledgeRepository repository;

    @Test
    void findAll_returnsAllDomains() {
        List<KnowledgeEntry> all = repository.findAll();

        assertThat(all).isNotEmpty();
        assertThat(all.stream().map(KnowledgeEntry::domain).distinct())
                .containsExactlyInAnyOrder(
                        "inventory", "payment", "settlement", "coupon",
                        "commerce", "spring-commerce");
    }

    @Test
    void findAll_totalCountMatchesYamlItems() {
        // YAML 지식 현황: inventory 8, payment 7, settlement 5, coupon 6, commerce 20, spring-commerce 20
        assertThat(repository.findAll()).hasSizeGreaterThanOrEqualTo(66);
    }

    @Test
    void findByDomain_inventory_returnsOnlyInventoryEntries() {
        List<KnowledgeEntry> entries = repository.findByDomain("inventory");

        assertThat(entries).isNotEmpty();
        assertThat(entries).allMatch(e -> "inventory".equals(e.domain()));
    }

    @Test
    void findByDomain_commerce_hasNonNullSummaryAndSections() {
        List<KnowledgeEntry> entries = repository.findByDomain("commerce");

        assertThat(entries).isNotEmpty();
        assertThat(entries).allMatch(e -> e.summary() != null && !e.summary().isBlank());
        assertThat(entries).allMatch(e -> e.sections().containsKey("invariants"));
        assertThat(entries).allMatch(e -> e.sections().containsKey("workflow"));
    }

    @Test
    void findByDomain_springCommerce_hasSpringGuidanceSections() {
        List<KnowledgeEntry> entries = repository.findByDomain("spring-commerce");

        assertThat(entries).isNotEmpty();
        assertThat(entries).allMatch(e -> e.sections().containsKey("spring-guidance"));
        assertThat(entries).allMatch(e -> e.sections().containsKey("avoid-patterns"));
    }

    @Test
    void findByDomain_simpleDomains_haveStructuredSchema() {
        for (String domain : List.of("inventory", "payment", "settlement", "coupon")) {
            List<KnowledgeEntry> entries = repository.findByDomain(domain);
            // 모든 항목은 summary를 가진다
            assertThat(entries)
                    .as("'%s' all entries must have summary", domain)
                    .allMatch(e -> e.summary() != null && !e.summary().isBlank());
            // checklist 카테고리가 아닌 항목은 guidance 섹션을 가진다
            assertThat(entries.stream().filter(e -> !"checklist".equals(e.category())).toList())
                    .as("'%s' non-checklist entries must have guidance section", domain)
                    .allMatch(e -> e.sections().containsKey("guidance"));
            // checklist 카테고리 항목은 체크리스트 아이템을 가진다
            assertThat(entries.stream().filter(e -> "checklist".equals(e.category())).toList())
                    .as("'%s' checklist entries must have checklist items", domain)
                    .allMatch(e -> !e.checklist().isEmpty());
        }
    }

    @Test
    void findByCategory_inventory_concurrency_returnsMatchingEntries() {
        List<KnowledgeEntry> entries = repository.findByCategory("inventory", "concurrency");

        assertThat(entries).isNotEmpty();
        assertThat(entries).allMatch(e -> "concurrency".equals(e.category()));
    }

    @Test
    void allEntries_haveRequiredFields() {
        repository.findAll().forEach(entry -> {
            assertThat(entry.domain()).as("domain must not be blank").isNotBlank();
            assertThat(entry.id()).as("id must not be blank for %s", entry.domain()).isNotBlank();
            assertThat(entry.category()).as("category must not be blank for %s", entry.id()).isNotBlank();
            assertThat(entry.title()).as("title must not be blank for %s", entry.id()).isNotBlank();
            assertThat(entry.tags()).as("tags must not be null for %s", entry.id()).isNotNull();
        });
    }
}
