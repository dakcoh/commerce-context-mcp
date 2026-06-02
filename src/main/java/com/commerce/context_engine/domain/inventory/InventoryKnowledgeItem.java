package com.commerce.context_engine.domain.inventory;

import lombok.Data;
import java.util.List;

@Data
public class InventoryKnowledgeItem {
    private String id;
    private String category;
    private String title;
    private String content;
    private List<String> tags;
}
