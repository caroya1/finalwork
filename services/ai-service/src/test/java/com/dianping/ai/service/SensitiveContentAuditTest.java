package com.dianping.ai.service;

import com.dianping.ai.metrics.AiApiMetrics;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.util.SensitiveWordFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensitiveContentAuditTest {

    @Mock
    private DashScopeClient dashScopeClient;

    @Mock
    private SensitiveWordFilter sensitiveWordFilter;

    @Mock
    private AiApiMetrics aiApiMetrics;

    @InjectMocks
    private AiAuditService aiAuditService;

    @Test
    void testAiReturnsRejectionReason() throws Exception {
        String content = "测试内容";
        String aiReason = "AI动态生成的拒绝理由";
        
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success("{\"approved\": false, \"reason\": \"" + aiReason + "\"}"));

        AuditResult result = aiAuditService.auditSync(content, "COMMENT");

        assertNotNull(result);
        assertFalse(result.getApproved());
        assertNotNull(result.getReason(), "AI应返回拒绝理由");
        assertFalse(result.getReason().isEmpty(), "拒绝理由不应为空");
    }

    @Test
    void testAiReturnsApprovalWithoutReason() throws Exception {
        String content = "正常内容";
        
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success("{\"approved\": true, \"confidence\": 0.95}"));

        AuditResult result = aiAuditService.auditSync(content, "POST");

        assertNotNull(result);
        assertTrue(result.getApproved());
        assertNull(result.getReason(), "通过时理由应为空");
    }

    @Test
    void testAiReturnsReasonWithConfidence() throws Exception {
        String content = "测试内容";
        double confidence = 0.85;
        
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success(
                    "{\"approved\": false, \"reason\": \"违规内容\", \"confidence\": " + confidence + "}"
                ));

        AuditResult result = aiAuditService.auditSync(content, "POST");

        assertNotNull(result);
        assertFalse(result.getApproved());
        assertEquals(confidence, result.getConfidence(), 0.01);
    }

    @Test
    void testParseAiJsonResponse() throws Exception {
        String jsonResponse = "{\"approved\": false, \"reason\": \"AI判断的理由\"}";
        
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success(jsonResponse));

        AuditResult result = aiAuditService.auditSync("内容", "COMMENT");

        assertNotNull(result.getReason());
    }

    @Test
    void testAiResponseWithComplexReason() throws Exception {
        String complexReason = "该内容包含多种违规：1.脏话 2.广告 3.人身攻击";
        
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success(
                    "{\"approved\": false, \"reason\": \"" + complexReason + "\"}"
                ));

        AuditResult result = aiAuditService.auditSync("内容", "POST");

        assertEquals(complexReason, result.getReason());
    }

    @Test
    void testFallbackUsesDefaultReason() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenThrow(new RuntimeException("API error"));
        
        when(sensitiveWordFilter.containsSensitiveWord(anyString())).thenReturn(true);

        AuditResult result = aiAuditService.auditSync("内容", "COMMENT");

        assertNotNull(result);
        assertFalse(result.getApproved());
        assertNotNull(result.getReason());
    }

    @Test
    void testEmptyReasonHandling() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success("{\"approved\": false, \"reason\": \"\"}"));

        AuditResult result = aiAuditService.auditSync("内容", "POST");

        assertNotNull(result);
        assertFalse(result.getApproved());
    }

    @Test
    void testReasonNotNullForRejectedContent() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success("{\"approved\": false, \"reason\": \"任何理由\"}"));

        AuditResult result = aiAuditService.auditSync("任何内容", "POST");

        assertNotNull(result.getReason());
    }
}
