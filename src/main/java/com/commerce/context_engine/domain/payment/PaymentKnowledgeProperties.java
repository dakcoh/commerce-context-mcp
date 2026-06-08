package com.commerce.context_engine.domain.payment;

import com.commerce.context_engine.domain.SimpleKnowledgeItem;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "payment")
public class PaymentKnowledgeProperties {

    private List<SimpleKnowledgeItem> items;
}
