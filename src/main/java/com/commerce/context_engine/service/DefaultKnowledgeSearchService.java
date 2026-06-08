package com.commerce.context_engine.service;

import com.commerce.context_engine.core.KnowledgeEntry;
import com.commerce.context_engine.core.KnowledgeQuery;
import com.commerce.context_engine.core.KnowledgeRepository;
import com.commerce.context_engine.core.KnowledgeSearchResult;
import com.commerce.context_engine.core.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DefaultKnowledgeSearchService implements KnowledgeSearchService {

    private final KnowledgeRepository repository;

    @Override
    public List<KnowledgeSearchResult> search(KnowledgeQuery query) {
        List<KnowledgeEntry> candidates = candidates(query);

        if (isBlank(query.keyword())) {
            return candidates.stream()
                    .map(e -> new KnowledgeSearchResult(e, List.of(), 0))
                    .toList();
        }

        String keyword = normalize(query.keyword());
        return candidates.stream()
                .map(e -> toResult(e, keyword))
                .filter(r -> r.score() > 0)
                .sorted(Comparator.comparingInt(KnowledgeSearchResult::score).reversed())
                .limit(query.limit() > 0 ? query.limit() : Long.MAX_VALUE)
                .toList();
    }

    @Override
    public List<KnowledgeEntry> getByDomain(String domain) {
        return repository.findByDomain(domain);
    }

    @Override
    public List<KnowledgeEntry> getByCategory(String domain, String category) {
        return repository.findByCategory(domain, category);
    }

    // ── scoring ──────────────────────────────────────────────────────────────

    private KnowledgeSearchResult toResult(KnowledgeEntry entry, String keyword) {
        List<String> matchedFields = new ArrayList<>();
        int score = 0;

        if (contains(entry.title(), keyword)) {
            matchedFields.add("title");
            score += 5;
        }
        if (entry.tags().stream().anyMatch(t -> contains(t, keyword))) {
            matchedFields.add("tags");
            score += 4;
        }
        if (contains(entry.category(), keyword)) {
            matchedFields.add("category");
            score += 3;
        }
        if (contains(entry.summary(), keyword)) {
            matchedFields.add("summary");
            score += 2;
        }
        if (contains(entry.content(), keyword)) {
            matchedFields.add("content");
            score += 1;
        }
        for (Map.Entry<String, List<String>> section : entry.sections().entrySet()) {
            if (section.getValue().stream().anyMatch(v -> contains(v, keyword))) {
                matchedFields.add("sections." + section.getKey());
                score += 1;
            }
        }
        if (entry.checklist().stream().anyMatch(c -> contains(c, keyword))) {
            matchedFields.add("checklist");
            score += 1;
        }

        return new KnowledgeSearchResult(entry, List.copyOf(matchedFields), score);
    }

    // ── filtering ────────────────────────────────────────────────────────────

    private List<KnowledgeEntry> candidates(KnowledgeQuery query) {
        if (query.domain() != null && query.category() != null) {
            return repository.findByCategory(query.domain(), query.category());
        }
        if (query.domain() != null) {
            return repository.findByDomain(query.domain());
        }
        return repository.findAll();
    }

    // ── utils ─────────────────────────────────────────────────────────────────

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return normalize(value).isEmpty();
    }

    private static boolean contains(String value, String normalizedKeyword) {
        return normalize(value).contains(normalizedKeyword);
    }
}
