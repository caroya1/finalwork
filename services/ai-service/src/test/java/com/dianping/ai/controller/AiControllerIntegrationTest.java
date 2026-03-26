package com.dianping.ai.controller;

import com.dianping.common.dto.AuditRequest;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.dto.RecommendRequest;
import com.dianping.common.dto.ShopDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.cloud.nacos.config.enabled=false",
    "spring.cloud.nacos.discovery.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "ai.dashscope.api-key=test-key"
})
class AiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAuditEndpointExists() throws Exception {
        AuditRequest request = new AuditRequest();
        request.setContent("测试内容");
        request.setType("POST");
        request.setTargetId(1L);

        mockMvc.perform(post("/api/ai/audit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").exists());
    }

    @Test
    void testRecommendEndpointExists() throws Exception {
        RecommendRequest request = new RecommendRequest();
        request.setQuery("推荐一家餐厅");
        request.setCity("北京");
        request.setUserId(1L);

        mockMvc.perform(post("/api/ai/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testMetricsEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }
}
