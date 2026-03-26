package com.dianping.ai.util;

import com.dianping.ai.service.DashScopeClient;
import com.dianping.ai.service.DashScopeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IntentParser {

    private static final Logger logger = LoggerFactory.getLogger(IntentParser.class);

    private static final String INTENT_PROMPT_TEMPLATE = 
            "用户搜索: %s\n" +
            "请解析用户意图，提取以下信息并以JSON返回:\n" +
            "{\n" +
            "  \"cuisine\": \"菜系，如川菜、粤菜、西餐\",\n" +
            "  \"priceRange\": \"价格范围，如50-100\",\n" +
            "  \"scene\": \"场景，如约会、聚餐、商务\",\n" +
            "  \"location\": \"位置偏好\"\n" +
            "}\n" +
            "如果某项信息无法从搜索词中提取，请返回null。只返回JSON，不要其他解释。";

    private static final Pattern JSON_PATTERN = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL);
    private static final int MAX_RETRIES = 2;

    private final DashScopeClient dashScopeClient;
    private final ObjectMapper objectMapper;

    public IntentParser(DashScopeClient dashScopeClient, ObjectMapper objectMapper) {
        this.dashScopeClient = dashScopeClient;
        this.objectMapper = objectMapper;
    }

    public ParsedIntent parse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return ParsedIntent.empty();
        }

        return parseWithRetry(query, 0);
    }

    private ParsedIntent parseWithRetry(String query, int retryCount) {
        try {
            String prompt = String.format(INTENT_PROMPT_TEMPLATE, query);
            DashScopeResponse response = dashScopeClient.generate(prompt);

            if (response.isSuccess() && response.getText() != null) {
                return parseJsonResponse(response.getText());
            } else {
                logger.warn("DashScope API call failed: {}", response.getMessage());
                if (retryCount < MAX_RETRIES) {
                    return parseWithRetry(query, retryCount + 1);
                }
                return ParsedIntent.empty();
            }
        } catch (Exception e) {
            logger.error("Intent parsing failed for query: {}", query, e);
            if (retryCount < MAX_RETRIES) {
                return parseWithRetry(query, retryCount + 1);
            }
            return ParsedIntent.empty();
        }
    }

    private ParsedIntent parseJsonResponse(String responseText) {
        try {
            String jsonStr = extractJson(responseText);
            if (jsonStr == null) {
                logger.warn("No JSON found in response: {}", responseText);
                return ParsedIntent.empty();
            }

            JsonNode root = objectMapper.readTree(jsonStr);

            ParsedIntent intent = new ParsedIntent();
            intent.setCuisine(extractStringValue(root, "cuisine"));
            intent.setPriceRange(extractStringValue(root, "priceRange"));
            intent.setScene(extractStringValue(root, "scene"));
            intent.setLocation(extractStringValue(root, "location"));
            intent.setConfidence(calculateConfidence(intent));

            logger.debug("Parsed intent: {}", intent);
            return intent;
        } catch (Exception e) {
            logger.error("Failed to parse JSON response: {}", responseText, e);
            return ParsedIntent.empty();
        }
    }

    private String extractJson(String text) {
        Matcher matcher = JSON_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractStringValue(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode != null && !fieldNode.isNull()) {
            String value = fieldNode.asText();
            if (value != null && !value.equals("null") && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private double calculateConfidence(ParsedIntent intent) {
        int fields = 0;
        if (intent.getCuisine() != null) fields++;
        if (intent.getPriceRange() != null) fields++;
        if (intent.getScene() != null) fields++;
        if (intent.getLocation() != null) fields++;

        return switch (fields) {
            case 0 -> 0.0;
            case 1 -> 0.25;
            case 2 -> 0.5;
            case 3 -> 0.75;
            case 4 -> 1.0;
            default -> 0.5;
        };
    }
}
