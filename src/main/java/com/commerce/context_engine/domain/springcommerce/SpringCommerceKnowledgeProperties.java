package com.commerce.context_engine.domain.springcommerce;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "spring-commerce")
public class SpringCommerceKnowledgeProperties {

    private List<Item> items;

    @Data
    public static class Item {
        private String id;
        private String category;
        private String title;
        private String summary;
        private String businessContext;
        private List<String> springGuidance;
        private List<String> avoidPatterns;
        private List<String> checklist;
        private List<String> tags;
    }
}
