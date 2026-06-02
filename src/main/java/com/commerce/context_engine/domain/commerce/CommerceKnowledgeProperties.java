package com.commerce.context_engine.domain.commerce;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "commerce")
public class CommerceKnowledgeProperties {

    private List<Item> items;

    @Data
    public static class Item {
        private String id;
        private String category;
        private String title;
        private String summary;
        private String businessContext;
        private List<String> invariants;
        private List<String> workflow;
        private List<String> technicalGuidance;
        private List<String> failureScenarios;
        private List<String> checklist;
        private List<String> tags;
    }
}
