package com.commerce.context_engine.service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

final class KnowledgeSearchSupport {

    private KnowledgeSearchSupport() {
    }

    static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    static boolean hasSearchKeyword(String keyword) {
        return !normalize(keyword).isEmpty();
    }

    static boolean containsNormalized(String value, String normalizedKeyword) {
        return normalize(value).contains(normalizedKeyword);
    }

    static <T> List<T> safeList(List<T> items) {
        return items == null ? List.of() : items;
    }

    static Stream<String> safeStream(List<String> items) {
        return safeList(items).stream();
    }

    static String missingKeywordMessage() {
        return "Please provide a non-blank search keyword.";
    }
}
