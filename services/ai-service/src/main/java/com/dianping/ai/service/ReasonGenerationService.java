package com.dianping.ai.service;

import com.dianping.common.dto.GenerateReasonRequest;
import com.dianping.common.dto.GenerateReasonResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ReasonGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReasonGenerationService.class);
    
    private final DashScopeClient dashScopeClient;
    private final ObjectMapper objectMapper;
    
    public ReasonGenerationService(DashScopeClient dashScopeClient) {
        this.dashScopeClient = dashScopeClient;
        this.objectMapper = new ObjectMapper();
    }
    
    public GenerateReasonResponse generateReason(GenerateReasonRequest request) {
        try {
            String prompt = buildPrompt(request);
            DashScopeResponse aiResponse = dashScopeClient.generate(prompt);
            
            if (aiResponse.isSuccess()) {
                return parseAIResponse(aiResponse.getText());
            } else {
                logger.warn("AI生成推荐理由失败: {}", aiResponse.getMessage());
                return buildFallbackResponse(request);
            }
        } catch (Exception e) {
            logger.error("生成推荐理由异常", e);
            return buildFallbackResponse(request);
        }
    }
    
    private String buildPrompt(GenerateReasonRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的美食推荐助手。请为以下餐厅生成推荐理由和亮点。\n\n");
        prompt.append("餐厅信息：\n");
        prompt.append("- 店名：").append(request.getShopName()).append("\n");
        prompt.append("- 品类：").append(request.getCategory()).append("\n");
        prompt.append("- 评分：").append(request.getRating()).append("/5\n");
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            prompt.append("- 标签：").append(request.getTags()).append("\n");
        }
        prompt.append("- 推荐排名：第").append(request.getRank() + 1).append("名\n");
        prompt.append("- 用户场景：").append(request.getUserQuery()).append("\n\n");
        
        prompt.append("请根据以上信息，生成：\n");
        prompt.append("1. 一句简短有力的推荐理由（20-40字），说明为什么这家餐厅适合用户的场景\n");
        prompt.append("2. 2-3个店铺亮点，每个亮点15-25字\n\n");
        
        prompt.append("请严格按以下JSON格式返回：\n");
        prompt.append("{\n");
        prompt.append("  \"recommendReason\": \"推荐理由...\",\n");
        prompt.append("  \"highlights\": [\"亮点1\", \"亮点2\", \"亮点3\"]\n");
        prompt.append("}");
        
        return prompt.toString();
    }
    
    private GenerateReasonResponse parseAIResponse(String content) {
        GenerateReasonResponse response = new GenerateReasonResponse();
        
        try {
            // 提取JSON部分
            String json = extractJson(content);
            JsonNode root = objectMapper.readTree(json);
            
            if (root.has("recommendReason")) {
                response.setRecommendReason(root.get("recommendReason").asText());
            }
            
            if (root.has("highlights") && root.get("highlights").isArray()) {
                List<String> highlights = objectMapper.convertValue(
                    root.get("highlights"), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                response.setHighlights(highlights);
            }
        } catch (Exception e) {
            logger.warn("解析AI响应失败: {}", content, e);
            // 尝试从文本中提取
            response = extractFromText(content);
        }
        
        return response;
    }
    
    private String extractJson(String content) {
        // 尝试提取JSON部分
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }
    
    private GenerateReasonResponse extractFromText(String content) {
        GenerateReasonResponse response = new GenerateReasonResponse();
        
        // 简单提取推荐理由（假设包含"推荐理由"字样）
        if (content.contains("推荐理由")) {
            int start = content.indexOf("推荐理由");
            int end = content.indexOf("\n", start);
            if (end > start) {
                response.setRecommendReason(content.substring(start, end).replace("推荐理由", "").replace("：", "").replace(":", "").trim());
            }
        }
        
        // 默认亮点
        response.setHighlights(Arrays.asList("口味正宗，深受好评", "环境舒适，服务周到"));
        
        return response;
    }
    
    private GenerateReasonResponse buildFallbackResponse(GenerateReasonRequest request) {
        GenerateReasonResponse response = new GenerateReasonResponse();
        
        // 根据排名生成不同的推荐理由
        String[] reasons = {
            "最符合您的需求，强烈推荐",
            "性价比之选，值得一试",
            "品质保证，不错的选择"
        };
        
        int rank = Math.min(request.getRank(), reasons.length - 1);
        String reason = reasons[rank];
        
        if (request.getRating() != null && request.getRating() >= 4.5) {
            reason += "，评分高达" + String.format("%.1f", request.getRating()) + "分";
        }
        
        response.setRecommendReason(reason);
        
        // 根据品类生成亮点
        String category = request.getCategory();
        if (category != null) {
            if (category.contains("火锅")) {
                response.setHighlights(Arrays.asList("锅底正宗，食材新鲜", "氛围热闹，适合聚餐"));
            } else if (category.contains("日料")) {
                response.setHighlights(Arrays.asList("食材新鲜，制作精良", "环境雅致，体验极佳"));
            } else if (category.contains("西餐")) {
                response.setHighlights(Arrays.asList("环境优雅，适合约会", "菜品精致，摆盘用心"));
            } else {
                response.setHighlights(Arrays.asList("口味地道，深受好评", "服务周到，环境舒适"));
            }
        } else {
            response.setHighlights(Arrays.asList("口碑良好，值得一试", "位置便利，交通方便"));
        }
        
        return response;
    }
}
