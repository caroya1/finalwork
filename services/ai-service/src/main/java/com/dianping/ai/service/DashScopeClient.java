package com.dianping.ai.service;

import com.dianping.ai.config.AiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class DashScopeClient {

    @Autowired
    private AiConfig aiConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DashScopeResponse generate(String prompt) throws Exception {
        String apiKey = aiConfig.getApiKey();
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("your-api-key")) {
            throw new IllegalStateException("DashScope API key not configured. Please set DASHSCOPE_API_KEY environment variable or update Nacos config.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        requestBody.put("messages", new Object[]{message});

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                aiConfig.getBaseUrl() + "/chat/completions",
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String content = root.path("choices").get(0).path("message").path("content").asText();
                return DashScopeResponse.success(content);
            } else {
                return DashScopeResponse.failure("API call failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return DashScopeResponse.failure("Exception: " + e.getMessage());
        }
    }
}