package com.dianping.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI DashScope 配置类
 * 阿里云通义千问DashScope SDK配置
 */
@Component
@ConfigurationProperties(prefix = "ai.dashscope")
public class AiConfig {

    /**
     * DashScope API Key
     */
    private String apiKey;

    /**
     * Base URL for DashScope API
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * Default model to use
     */
    private String model = "qwen-plus";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}