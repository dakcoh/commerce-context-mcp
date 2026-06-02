package com.commerce.context_engine.config;

import com.commerce.context_engine.domain.coupon.CouponKnowledgeProperties;
import com.commerce.context_engine.domain.inventory.InventoryKnowledgeProperties;
import com.commerce.context_engine.domain.payment.PaymentKnowledgeProperties;
import com.commerce.context_engine.domain.settlement.SettlementKnowledgeProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@EnableConfigurationProperties({
        InventoryKnowledgeProperties.class,
        PaymentKnowledgeProperties.class,
        SettlementKnowledgeProperties.class,
        CouponKnowledgeProperties.class
})
@PropertySources({
        @PropertySource(value = "classpath:knowledge/inventory.yml",  factory = YamlPropertySourceFactory.class),
        @PropertySource(value = "classpath:knowledge/payment.yml",    factory = YamlPropertySourceFactory.class),
        @PropertySource(value = "classpath:knowledge/settlement.yml", factory = YamlPropertySourceFactory.class),
        @PropertySource(value = "classpath:knowledge/coupon.yml",     factory = YamlPropertySourceFactory.class)
})
public class KnowledgeConfig {
}
