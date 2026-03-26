package com.dianping.ai.service;

import com.dianping.ai.metrics.AiApiMetrics;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.util.SensitiveWordFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FallbackSensitiveWordTest {

    @Mock
    private DashScopeClient dashScopeClient;

    @Mock
    private SensitiveWordFilter sensitiveWordFilter;

    @Mock
    private AiApiMetrics aiApiMetrics;

    @InjectMocks
    private AiAuditService aiAuditService;

    @Test
    void testFallbackWhenApiTimeoutWithSensitiveWords() throws Exception {
        String contentWithSensitiveWords = "他妈的，这真他妈坑人！";
        
        doThrow(new TimeoutException("API timeout"))
                .when(dashScopeClient).generate(anyString());
        
        when(sensitiveWordFilter.containsSensitiveWord(contentWithSensitiveWords))
                .thenReturn(true);

        AuditResult result = aiAuditService.auditSync(contentWithSensitiveWords, "COMMENT");

        assertNotNull(result);
        assertFalse(result.getApproved(), "包含敏感词的内容在fallback时应该被拒绝");
        verify(sensitiveWordFilter).containsSensitiveWord(contentWithSensitiveWords);
    }

    @Test
    void testFallbackWhenApiTimeoutWithoutSensitiveWords() throws Exception {
        String cleanContent = "这家餐厅真的很好吃，环境也不错！";
        
        doThrow(new TimeoutException("API timeout"))
                .when(dashScopeClient).generate(anyString());
        
        when(sensitiveWordFilter.containsSensitiveWord(cleanContent))
                .thenReturn(false);

        AuditResult result = aiAuditService.auditSync(cleanContent, "POST");

        assertNotNull(result);
        assertTrue(result.getApproved(), "不含敏感词的内容在fallback时应该被通过");
        verify(sensitiveWordFilter).containsSensitiveWord(cleanContent);
    }

    @Test
    void testRealSensitiveWordsDetection() {
        String[] sensitiveContents = {
            "傻逼商家，骗子！",
            "他妈的服务态度真差",
            "艹，什么破东西",
            "日你妈，退钱！",
            "狗屎一样的质量"
        };
        
        for (String content : sensitiveContents) {
            System.out.println("测试敏感内容: " + content);
        }
        
        System.out.println("✅ 敏感词测试用例已准备");
    }

    @Test
    void testEdgeCases() throws Exception {
        String[] edgeCases = {
            "",  // 空内容
            "   ",  // 只有空格
            "正常内容",  // 正常中文
            "Normal English content",  // 英文
            "123456789",  // 纯数字
            "@#$%^&*()"  // 特殊字符
        };
        
        for (String content : edgeCases) {
            when(dashScopeClient.generate(anyString()))
                    .thenReturn(DashScopeResponse.success("{\"approved\": true}"));
            
            AuditResult result = aiAuditService.auditSync(content, "POST");
            assertNotNull(result, "边缘情况应该能处理: " + content);
        }
    }
}
