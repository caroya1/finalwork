package com.dianping.common.config;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 配置 - 熔断降级、限流配置
 */
@Configuration
public class SentinelConfig {

    public SentinelConfig() {
        initFlowRules();
        initDegradeRules();
    }
    
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        FlowRule orderRule = new FlowRule("order-service");
        orderRule.setGrade(1);
        orderRule.setCount(100);
        rules.add(orderRule);
        
        FlowRule merchantRule = new FlowRule("merchant-service");
        merchantRule.setGrade(1);
        merchantRule.setCount(50);
        rules.add(merchantRule);
        
        FlowRule userRule = new FlowRule("user-service");
        userRule.setGrade(1);
        userRule.setCount(100);
        rules.add(userRule);
        
        FlowRuleManager.loadRules(rules);
    }
    
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        
        DegradeRule orderRule = new DegradeRule("order-service");
        orderRule.setGrade(1);
        orderRule.setCount(0.5);
        orderRule.setTimeWindow(10);
        orderRule.setMinRequestAmount(10);
        orderRule.setStatIntervalMs(10000);
        rules.add(orderRule);
        
        DegradeRule merchantRule = new DegradeRule("merchant-service");
        merchantRule.setGrade(1);
        merchantRule.setCount(0.5);
        merchantRule.setTimeWindow(10);
        merchantRule.setMinRequestAmount(10);
        rules.add(merchantRule);
        
        DegradeRuleManager.loadRules(rules);
    }
}
