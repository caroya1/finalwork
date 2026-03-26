package com.dianping.ai.service;

import com.dianping.common.dto.AuditResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.sql.init.mode=never",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "ai.dashscope.api-key=test-key"
})
class RealAiAuditIntegrationTest {

    @Autowired
    private AiAuditService aiAuditService;

    @Test
    void testRealAiAuditProfanity() {
        String profanityContent = "他妈的，这个商家真垃圾！";
        
        AuditResult result = aiAuditService.auditSync(profanityContent, "COMMENT");
        
        System.out.println("AI审核结果: " + result);
        System.out.println("是否通过: " + result.getApproved());
        System.out.println("拒绝理由: " + result.getReason());
        System.out.println("置信度: " + result.getConfidence());
        
        assertNotNull(result);
        assertFalse(result.getApproved(), "脏话应该被拒绝");
        assertNotNull(result.getReason(), "AI应返回拒绝理由");
        assertFalse(result.getReason().isEmpty(), "拒绝理由不应为空");
    }

    @Test
    void testRealAiAuditNormalContent() {
        String normalContent = "这家餐厅环境很好，菜品味道不错，推荐给大家！";
        
        AuditResult result = aiAuditService.auditSync(normalContent, "POST");
        
        System.out.println("AI审核结果: " + result);
        System.out.println("是否通过: " + result.getApproved());
        System.out.println("拒绝理由: " + result.getReason());
        System.out.println("置信度: " + result.getConfidence());
        
        assertNotNull(result);
        assertTrue(result.getApproved(), "正常内容应该通过");
    }

    @Test
    void testRealAiAuditPoliticalContent() {
        String politicalContent = "某事件真相曝光，政府隐瞒不报";
        
        AuditResult result = aiAuditService.auditSync(politicalContent, "POST");
        
        System.out.println("AI审核结果: " + result);
        System.out.println("是否通过: " + result.getApproved());
        System.out.println("拒绝理由: " + result.getReason());
        
        assertNotNull(result);
        assertNotNull(result.getReason());
    }

    @Test
    void testRealAiAuditAdvertising() {
        String adContent = "加微信123456，日赚万元，稳赚不赔！";
        
        AuditResult result = aiAuditService.auditSync(adContent, "COMMENT");
        
        System.out.println("AI审核结果: " + result);
        System.out.println("是否通过: " + result.getApproved());
        System.out.println("拒绝理由: " + result.getReason());
        
        assertNotNull(result);
    }

    @Test
    void testRealAiAuditPersonalAttack() {
        String attackContent = "这个用户是个傻逼，大家别理他";
        
        AuditResult result = aiAuditService.auditSync(attackContent, "COMMENT");
        
        System.out.println("AI审核结果: " + result);
        System.out.println("是否通过: " + result.getApproved());
        System.out.println("AI返回的拒绝理由: " + result.getReason());
        
        assertNotNull(result);
        assertFalse(result.getApproved(), "人身攻击应该被拒绝");
        assertNotNull(result.getReason(), "AI应返回具体理由");
    }

    @Test
    void testRealAiAuditMultipleTypes() {
        String[] testContents = {
            "正常的美食推荐内容",
            "这个破地方真垃圾",
            "加我微信买毒品",
            "政府不作为，真相被隐瞒",
            "这家餐厅真不错，五星好评"
        };
        
        for (String content : testContents) {
            AuditResult result = aiAuditService.auditSync(content, "POST");
            System.out.println("\n内容: " + content);
            System.out.println("结果: " + (result.getApproved() ? "通过" : "拒绝"));
            System.out.println("理由: " + result.getReason());
            assertNotNull(result);
        }
    }
}
