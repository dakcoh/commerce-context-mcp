package com.commerce.context_engine.domain.inventory;

import lombok.Data;
import java.util.List;

@Data
public class InventoryKnowledge {
    private List<InventoryKnowledgeItem> items;
}
