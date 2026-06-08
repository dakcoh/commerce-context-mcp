package com.commerce.context_engine.domain.inventory;

import com.commerce.context_engine.domain.SimpleKnowledgeItem;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "inventory")
public class InventoryKnowledgeProperties {

    private List<SimpleKnowledgeItem> items;
}
