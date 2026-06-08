package com.commerce.context_engine.domain.coupon;

import com.commerce.context_engine.domain.SimpleKnowledgeItem;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "coupon")
public class CouponKnowledgeProperties {

    private List<SimpleKnowledgeItem> items;
}
