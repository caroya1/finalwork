package com.dianping.ai.service;

import com.dianping.ai.metrics.AiApiMetrics;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.util.SensitiveWordFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiAuditServiceTest {

    @Mock
    private DashScopeClient dashScopeClient;

    @Mock
    private SensitiveWordFilter sensitiveWordFilter;

    @Mock
    private AiApiMetrics aiApiMetrics;

    @InjectMocks
    private AiAuditService aiAuditService;

    private static final String TEST_TEXT = "这是一段正常的测试文本";
    private static final String AUDIT_TYPE = "post";

    @BeforeEach
    void setUp() {
    }

    @Test
    void testNormalAuditPass() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success("{\"approved\": true, \"confidence\": 0.95}"));

        AuditResult result = aiAuditService.auditSync(TEST_TEXT, AUDIT_TYPE);

        assertNotNull(result);
        assertTrue(result.getApproved(), "审核应该通过");
        assertNotNull(result.getConfidence());
        assertTrue(result.getConfidence() > 0.8, "置信度应该大于0.8");
    }

    @Test
    void testAuditRejectSensitiveContent() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success("{\"approved\": false, \"reason\": \"包含敏感内容\"}"));

        AuditResult result = aiAuditService.auditSync(TEST_TEXT, AUDIT_TYPE);

        assertNotNull(result);
        assertFalse(result.getApproved(), "审核应该拒绝");
        assertNotNull(result.getReason(), "拒绝原因不应该为空");
        assertTrue(result.getReason().contains("敏感") || result.getReason().contains("未通过"), 
                "拒绝原因应该包含敏感内容或审核未通过");
    }

    @Test
    void testAuditTimeout() throws Exception {
        doThrow(new java.util.concurrent.TimeoutException("Request timeout"))
                .when(dashScopeClient).generate(anyString());

        when(sensitiveWordFilter.containsSensitiveWord(TEST_TEXT)).thenReturn(false);

        AuditResult result = aiAuditService.auditSync(TEST_TEXT, AUDIT_TYPE);

        assertNotNull(result, "超时时应返回fallback结果");
        verify(sensitiveWordFilter, atLeastOnce()).containsSensitiveWord(TEST_TEXT);
    }

    @Test
    void testAuditFallback() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenThrow(new RuntimeException("API error"));

        when(sensitiveWordFilter.containsSensitiveWord(TEST_TEXT)).thenReturn(false);

        AuditResult result = aiAuditService.auditSync(TEST_TEXT, AUDIT_TYPE);

        assertNotNull(result, "API异常时应返回fallback结果");
        assertNotNull(result.getApproved(), "应返回审核结果");
        verify(sensitiveWordFilter).containsSensitiveWord(TEST_TEXT);
    }

    @Test
    void testRetryMechanism() throws Exception {
        doThrow(new java.util.concurrent.TimeoutException("First call timeout"))
                .doReturn(DashScopeResponse.success("{\"approved\": true}"))
                .when(dashScopeClient).generate(anyString());

        AuditResult result = aiAuditService.auditSync(TEST_TEXT, AUDIT_TYPE);

        assertNotNull(result);
        assertTrue(result.getApproved(), "重试成功后应返回通过结果");
        verify(dashScopeClient, times(2)).generate(anyString());
    }

    @Test
    void testCircuitBreakerFallback() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenThrow(new RuntimeException("Circuit breaker open"));

        when(sensitiveWordFilter.containsSensitiveWord(TEST_TEXT)).thenReturn(false);

        AuditResult result = aiAuditService.audit(TEST_TEXT, AUDIT_TYPE);

        assertNotNull(result, "Circuit breaker fallback should return result");
        assertTrue(result.getApproved(), "Fallback should approve non-sensitive content");
        verify(sensitiveWordFilter).containsSensitiveWord(TEST_TEXT);
    }
}