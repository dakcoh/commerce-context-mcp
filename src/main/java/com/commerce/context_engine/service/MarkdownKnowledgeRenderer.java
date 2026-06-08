package com.commerce.context_engine.service;

import com.commerce.context_engine.core.KnowledgeEntry;
import com.commerce.context_engine.core.KnowledgeRenderer;
import com.commerce.context_engine.core.KnowledgeSearchResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MarkdownKnowledgeRenderer implements KnowledgeRenderer {

    private static final Map<String, String> SECTION_HEADERS = Map.of(
            "guidance",          "### 구현 가이드",
            "invariants",        "### 반드시 지켜야 할 원칙",
            "workflow",          "### 권장 흐름",
            "technical-guidance","### 기술 구현 참고",
            "failure-scenarios", "### 실패 시나리오",
            "spring-guidance",   "### Java Spring 구현 가이드",
            "avoid-patterns",    "### 피해야 할 패턴"
    );

    @Override
    public String renderAll(List<KnowledgeEntry> entries) {
        return entries.stream()
                .map(this::renderRich)
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    /** 단일 항목 렌더링 — 구현체 단위 테스트용 헬퍼. */
    public String render(KnowledgeEntry entry) {
        return renderRich(entry);
    }

    @Override
    public String renderSearchResults(List<KnowledgeSearchResult> results) {
        return renderAll(results.stream().map(KnowledgeSearchResult::entry).toList());
    }

    @Override
    public String renderChecklist(List<KnowledgeEntry> entries) {
        return entries.stream()
                .map(this::renderChecklistEntry)
                .collect(Collectors.joining("\n\n"));
    }

    // ── entry renderers ───────────────────────────────────────────────────────

    private String renderRich(KnowledgeEntry entry) {
        StringBuilder sb = new StringBuilder();

        sb.append("## ").append(entry.title()).append("\n");
        sb.append("- knowledgeId: `").append(entry.id()).append("`\n");
        sb.append("- category: `").append(entry.category()).append("`\n");
        sb.append("- summary: ").append(entry.summary());

        if (!safe(entry.content()).isBlank()) {
            sb.append("\n\n### 이커머스 맥락\n").append(safe(entry.content()));
        }

        entry.sections().forEach((key, values) -> {
            if (values.isEmpty()) return;
            String header = SECTION_HEADERS.getOrDefault(key, "### " + key);
            sb.append("\n\n").append(header).append("\n").append(renderSectionValues(key, values));
        });

        if (!entry.checklist().isEmpty()) {
            sb.append("\n\n### 검토 체크리스트\n").append(checkboxList(entry.checklist()));
        }

        return sb.toString().trim();
    }

    private String renderChecklistEntry(KnowledgeEntry entry) {
        if (entry.checklist().isEmpty()) {
            return "## " + entry.title();
        }
        return "## " + entry.title() + "\n"
                + "- knowledgeId: `" + entry.id() + "`\n"
                + checkboxList(entry.checklist());
    }

    // ── section formatting ────────────────────────────────────────────────────

    private String renderSectionValues(String key, List<String> values) {
        return "workflow".equals(key) ? numberedList(values) : bulletList(values);
    }

    private String bulletList(List<String> items) {
        return items.stream().map(i -> "- " + i).collect(Collectors.joining("\n"));
    }

    private String numberedList(List<String> items) {
        return IntStream.range(0, items.size())
                .mapToObj(i -> (i + 1) + ". " + items.get(i))
                .collect(Collectors.joining("\n"));
    }

    private String checkboxList(List<String> items) {
        return items.stream().map(i -> "- [ ] " + i).collect(Collectors.joining("\n"));
    }

    // ── utils ─────────────────────────────────────────────────────────────────

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
