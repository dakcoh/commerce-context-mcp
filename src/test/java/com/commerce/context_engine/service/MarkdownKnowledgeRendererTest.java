package com.commerce.context_engine.service;

import com.commerce.context_engine.core.KnowledgeEntry;
import com.commerce.context_engine.core.KnowledgeSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownKnowledgeRendererTest {

    MarkdownKnowledgeRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new MarkdownKnowledgeRenderer();
    }

    // ── structured simple-domain entry ───────────────────────────────────────

    @Test
    void render_structuredEntry_containsTitleAndSummary() {
        KnowledgeEntry entry = structuredSimpleEntry("재고 예약", "예약 단계 요약");

        String result = renderer.render(entry);

        assertThat(result).contains("## 재고 예약");
        assertThat(result).contains("예약 단계 요약");
    }

    @Test
    void render_structuredEntry_hasMetadataAndGuidanceSection() {
        String result = renderer.render(structuredSimpleEntry("제목", "요약"));

        assertThat(result).contains("knowledgeId");
        assertThat(result).contains("category");
        assertThat(result).contains("### 구현 가이드");
    }

    // ── rich entry (commerce) ─────────────────────────────────────────────────

    @Test
    void render_richEntry_containsMetadata() {
        KnowledgeEntry entry = commerceEntry();

        String result = renderer.render(entry);

        assertThat(result).contains("## 결제 원장");
        assertThat(result).contains("knowledgeId: `commerce-payment-ledger`");
        assertThat(result).contains("category: `payment`");
        assertThat(result).contains("summary: 결제 거래 원장");
    }

    @Test
    void render_richEntry_sectionWorkflowIsNumbered() {
        KnowledgeEntry entry = commerceEntry();

        String result = renderer.render(entry);

        assertThat(result).contains("1. 결제 요청 수신");
        assertThat(result).contains("2. 검증");
    }

    @Test
    void render_richEntry_sectionsUseBulletList() {
        KnowledgeEntry entry = commerceEntry();

        String result = renderer.render(entry);

        assertThat(result).contains("- 원칙1");
        assertThat(result).contains("- 원칙2");
    }

    @Test
    void render_richEntry_checklistIsCheckbox() {
        KnowledgeEntry entry = commerceEntry();

        String result = renderer.render(entry);

        assertThat(result).contains("- [ ] 체크항목1");
    }

    @Test
    void render_richEntry_sectionHeadersMapped() {
        KnowledgeEntry entry = commerceEntry();

        String result = renderer.render(entry);

        assertThat(result).contains("### 반드시 지켜야 할 원칙");
        assertThat(result).contains("### 권장 흐름");
        assertThat(result).contains("### 검토 체크리스트");
    }

    // ── spring-commerce entry ─────────────────────────────────────────────────

    @Test
    void render_springCommerceEntry_hasSpringHeaders() {
        KnowledgeEntry entry = springCommerceEntry();

        String result = renderer.render(entry);

        assertThat(result).contains("### Java Spring 구현 가이드");
        assertThat(result).contains("### 피해야 할 패턴");
    }

    // ── renderAll ─────────────────────────────────────────────────────────────

    @Test
    void renderAll_separatesEntriesWithDivider() {
        List<KnowledgeEntry> entries = List.of(
                structuredSimpleEntry("제목1", "요약1"),
                structuredSimpleEntry("제목2", "요약2"));

        String result = renderer.renderAll(entries);

        assertThat(result).contains("---");
        assertThat(result).contains("## 제목1");
        assertThat(result).contains("## 제목2");
    }

    // ── renderSearchResults ───────────────────────────────────────────────────

    @Test
    void renderSearchResults_rendersEntryContent() {
        List<KnowledgeSearchResult> results = List.of(
                new KnowledgeSearchResult(structuredSimpleEntry("제목", "요약 내용"), List.of("title"), 5));

        String result = renderer.renderSearchResults(results);

        assertThat(result).contains("## 제목");
        assertThat(result).contains("요약 내용");
    }

    // ── renderChecklist ───────────────────────────────────────────────────────

    @Test
    void renderChecklist_richEntry_usesChecklistField() {
        KnowledgeEntry entry = commerceEntry();

        String result = renderer.renderChecklist(List.of(entry));

        assertThat(result).contains("- [ ] 체크항목1");
        assertThat(result).contains("knowledgeId");
    }

    @Test
    void renderChecklist_structuredEntry_usesChecklistField() {
        KnowledgeEntry entry = new KnowledgeEntry(
                "inventory", "inv-checklist", "checklist", "재고 체크리스트",
                "체크리스트 요약", null,
                Map.of(), List.of("재고 예약 단계가 있는가?", "오버셀링 방지 로직이 있는가?"),
                List.of("checklist"));

        String result = renderer.renderChecklist(List.of(entry));

        assertThat(result).contains("- [ ] 재고 예약 단계가 있는가?");
        assertThat(result).contains("- [ ] 오버셀링 방지 로직이 있는가?");
        assertThat(result).contains("knowledgeId");
    }

    // ── fixtures ──────────────────────────────────────────────────────────────

    private KnowledgeEntry structuredSimpleEntry(String title, String summary) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("guidance", List.of("가이드1", "가이드2"));
        sections.put("avoid-patterns", List.of("피할패턴1"));
        return new KnowledgeEntry("inventory", "inv-001", "lifecycle", title,
                summary, null, sections, List.of("체크항목1"), List.of("재고"));
    }

    private KnowledgeEntry commerceEntry() {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("invariants", List.of("원칙1", "원칙2"));
        sections.put("workflow", List.of("결제 요청 수신", "검증"));
        sections.put("technical-guidance", List.of());
        sections.put("failure-scenarios", List.of());

        return new KnowledgeEntry(
                "commerce", "commerce-payment-ledger", "payment", "결제 원장",
                "결제 거래 원장", "결제 맥락 설명",
                sections, List.of("체크항목1"), List.of("payment"));
    }

    private KnowledgeEntry springCommerceEntry() {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        sections.put("spring-guidance", List.of("가이드1"));
        sections.put("avoid-patterns", List.of("피할 패턴1"));

        return new KnowledgeEntry(
                "spring-commerce", "spring-tx-001", "transaction", "트랜잭션 경계",
                "짧은 트랜잭션 요약", "트랜잭션 맥락",
                sections, List.of("체크항목1"), List.of("transaction"));
    }
}
