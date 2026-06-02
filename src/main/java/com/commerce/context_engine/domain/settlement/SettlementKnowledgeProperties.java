package com.commerce.context_engine.domain.settlement;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "settlement")
public class SettlementKnowledgeProperties {

    private List<Item> items;

    @Data
    public static class Item {
        private String id;
        private String category;
        private String title;
        private String content;
        private List<String> tags;
    }
}
