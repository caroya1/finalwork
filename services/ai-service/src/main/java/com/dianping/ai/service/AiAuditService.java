package com.dianping.ai.service;

import com.dianping.ai.metrics.AiApiMetrics;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.util.SensitiveWordFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeoutException;

@Service
public class AiAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AiAuditService.class);
    private static final int MAX_RETRIES = 2;
    private static final String CIRCUIT_BREAKER_NAME = "aiAudit";
    private static final String RATE_LIMITER_NAME = "aiAudit";

    @Autowired
    private DashScopeClient dashScopeClient;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Autowired
    private AiApiMetrics aiApiMetrics;

    @Value("${ai.monitoring.cost-alert-threshold:100}")
    private double costAlertThreshold;

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "circuitBreakerFallback")
    @RateLimiter(name = RATE_LIMITER_NAME, fallbackMethod = "rateLimiterFallback")
    public AuditResult audit(String text, String auditType) {
        return audit(text, auditType, 0);
    }

    private AuditResult audit(String text, String auditType, int retryCount) {
        long startTime = System.currentTimeMillis();
        try {
            return callDashScopeApi(text, auditType, startTime);
        } catch (TimeoutException e) {
            long latency = System.currentTimeMillis() - startTime;
            aiApiMetrics.recordFailure(latency);
            logger.warn("AI audit timeout, retry {}/{}", retryCount + 1, MAX_RETRIES);
            if (retryCount < MAX_RETRIES) {
                return audit(text, auditType, retryCount + 1);
            }
            logger.error("AI audit timeout after {} retries, using fallback", MAX_RETRIES);
            return fallbackAudit(text, auditType);
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            aiApiMetrics.recordFailure(latency);
            logger.error("AI audit failed: {}, using fallback", e.getMessage());
            return fallbackAudit(text, auditType);
        }
    }

    private AuditResult callDashScopeApi(String text, String auditType, long startTime) throws TimeoutException, Exception {
        String prompt = "请审查以下内容是否合规，返回JSON格式：{\"approved\": true/false, \"reason\": \"原因\"}\n内容：" + text;

        DashScopeResponse response = dashScopeClient.generate(prompt);
        
        long latency = System.currentTimeMillis() - startTime;
        
        if (response.isSuccess()) {
            int inputTokens = estimateTokens(text);
            int outputTokens = estimateTokens(response.getText());
            aiApiMetrics.recordSuccess(latency, inputTokens, outputTokens);
            checkCostAlert();
            return parseApiResponse(response.getText(), auditType);
        } else {
            aiApiMetrics.recordFailure(latency);
            throw new Exception("API call failed: " + response.getMessage());
        }
    }

    private int estimateTokens(String text) {
        return text != null ? text.length() / 4 : 0;
    }

    private void checkCostAlert() {
        double dailyCost = aiApiMetrics.getDailyCostCny();
        if (dailyCost > costAlertThreshold) {
            logger.warn("ALERT: Daily AI API cost {} CNY exceeds threshold {} CNY", dailyCost, costAlertThreshold);
        }
    }

    private AuditResult parseApiResponse(String responseText, String auditType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseText);
            
            boolean approved = root.path("approved").asBoolean(true);
            String reason = approved ? null : root.path("reason").asText("审核未通过");
            double confidence = root.path("confidence").asDouble(approved ? 0.95 : 0.85);
            
            return new AuditResult(approved, reason, confidence, auditType);
        } catch (Exception e) {
            logger.warn("Failed to parse AI response: {}, using fallback parsing", responseText, e);
            boolean approved = !responseText.contains("\"approved\": false");
            return new AuditResult(approved, approved ? null : "审核未通过", approved ? 0.9 : 0.5, auditType);
        }
    }

    private AuditResult fallbackAudit(String text, String auditType) {
        logger.info("Using SensitiveWordFilter fallback for audit");
        
        boolean hasSensitive = sensitiveWordFilter.containsSensitiveWord(text);
        
        if (hasSensitive) {
            return new AuditResult(false, "包含敏感内容", 0.5, auditType);
        }
        
        return new AuditResult(true, null, 0.3, auditType);
    }

    private AuditResult circuitBreakerFallback(String text, String auditType, Throwable t) {
        logger.warn("Circuit breaker fallback triggered for AI audit: {}", t.getMessage());
        return fallbackAudit(text, auditType);
    }

    private AuditResult rateLimiterFallback(String text, String auditType, Throwable t) {
        logger.warn("Rate limiter fallback triggered for AI audit: {}", t.getMessage());
        return fallbackAudit(text, auditType);
    }

    public AuditResult auditSync(String text, String auditType) {
        return audit(text, auditType);
    }
}