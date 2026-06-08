package com.commerce.context_engine.domain.settlement;

import com.commerce.context_engine.domain.SimpleKnowledgeItem;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "settlement")
public class SettlementKnowledgeProperties {

    private List<SimpleKnowledgeItem> items;
}
