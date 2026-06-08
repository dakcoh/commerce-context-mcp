package com.commerce.context_engine.service;

import com.commerce.context_engine.core.KnowledgeEntry;
import com.commerce.context_engine.core.KnowledgeQuery;
import com.commerce.context_engine.core.KnowledgeRepository;
import com.commerce.context_engine.core.KnowledgeSearchResult;
import com.commerce.context_engine.core.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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

        List<String> terms = terms(query.keyword());
        return candidates.stream()
                .map(e -> toResult(e, terms))
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

    /**
     * 필드별로 "검색어 토큰 중 하나라도 매칭되면" 해당 필드 가중치를 1회 부여한다.
     * 단일 토큰 검색은 기존 스코어링과 동일하게 동작하고,
     * "웹훅 중복 결제"처럼 여러 단어로 된 자연어 질의는 각 토큰을 따로 매칭해
     * 부분 일치라도 결과가 노출되도록 한다.
     */
    private KnowledgeSearchResult toResult(KnowledgeEntry entry, List<String> terms) {
        List<String> matchedFields = new ArrayList<>();
        int score = 0;

        if (matchesAny(entry.title(), terms)) {
            matchedFields.add("title");
            score += 5;
        }
        if (entry.tags().stream().anyMatch(t -> matchesAny(t, terms))) {
            matchedFields.add("tags");
            score += 4;
        }
        if (matchesAny(entry.category(), terms)) {
            matchedFields.add("category");
            score += 3;
        }
        if (matchesAny(entry.summary(), terms)) {
            matchedFields.add("summary");
            score += 2;
        }
        if (matchesAny(entry.content(), terms)) {
            matchedFields.add("content");
            score += 1;
        }
        for (Map.Entry<String, List<String>> section : entry.sections().entrySet()) {
            if (section.getValue().stream().anyMatch(v -> matchesAny(v, terms))) {
                matchedFields.add("sections." + section.getKey());
                score += 1;
            }
        }
        if (entry.checklist().stream().anyMatch(c -> matchesAny(c, terms))) {
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

    /** 검색어를 공백 단위 토큰으로 분리한다. 빈 토큰은 제거하고 중복은 합친다. */
    private static List<String> terms(String keyword) {
        return Arrays.stream(normalize(keyword).split("\\s+"))
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String stripSpaces(String value) {
        return value.replaceAll("\\s+", "");
    }

    private static boolean isBlank(String value) {
        return normalize(value).isEmpty();
    }

    /**
     * 토큰 중 하나라도 값에 포함되면 true.
     * 정규화 문자열 그대로의 포함 검사와, 공백을 제거한 검사를 함께 수행해
     * "부분취소"가 "부분 취소"에도 매칭되도록 띄어쓰기 차이를 흡수한다.
     */
    private static boolean matchesAny(String value, List<String> terms) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return false;
        }
        String stripped = stripSpaces(normalized);
        for (String term : terms) {
            if (normalized.contains(term) || stripped.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
