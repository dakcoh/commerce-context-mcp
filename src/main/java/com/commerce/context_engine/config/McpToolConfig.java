package com.commerce.context_engine.config;

import com.commerce.context_engine.tool.CouponContextTool;
import com.commerce.context_engine.tool.InventoryContextTool;
import com.commerce.context_engine.tool.PaymentContextTool;
import com.commerce.context_engine.tool.SettlementContextTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    ToolCallbackProvider allToolCallbackProvider(InventoryContextTool inventoryTool,
                                                 PaymentContextTool paymentTool,
                                                 SettlementContextTool settlementTool,
                                                 CouponContextTool couponTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(inventoryTool, paymentTool, settlementTool, couponTool)
                .build();
    }
}
