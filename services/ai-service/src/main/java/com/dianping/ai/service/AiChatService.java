package com.dianping.ai.service;

import com.dianping.ai.client.ShopClient;
import com.dianping.common.dto.ShopDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiChatService {

    private static final Logger logger = LoggerFactory.getLogger(AiChatService.class);

    private final DashScopeClient dashScopeClient;
    private final ShopClient shopClient;
    private final ObjectMapper objectMapper;

    public AiChatService(DashScopeClient dashScopeClient, ShopClient shopClient, ObjectMapper objectMapper) {
        this.dashScopeClient = dashScopeClient;
        this.shopClient = shopClient;
        this.objectMapper = objectMapper;
    }

    public ChatRecommendationResult recommend(String userQuery, String city, Long userId) {
        logger.info("AI Chat recommendation - query: {}, city: {}", userQuery, city);

        try {
            List<ShopDTO> shops = shopClient.listByCity(city);
            if (shops == null || shops.isEmpty()) {
                return ChatRecommendationResult.noData("抱歉，该城市暂时没有店铺数据。");
            }

            String shopListJson = buildShopListJson(shops);
            String prompt = buildChatPrompt(userQuery, city, shopListJson);

            DashScopeResponse response = dashScopeClient.generate(prompt);

            if (!response.isSuccess() || response.getText() == null) {
                logger.error("AI API call failed: {}", response.getMessage());
                return fallbackRecommendation(userQuery, city, shops);
            }

            return parseAiResponse(response.getText(), userQuery, shops);

        } catch (Exception e) {
            logger.error("AI chat recommendation failed", e);
            return ChatRecommendationResult.error("AI服务暂时不可用，请稍后重试。");
        }
    }

    private String buildShopListJson(List<ShopDTO> shops) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < Math.min(shops.size(), 20); i++) {
            ShopDTO shop = shops.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"id\":" + shop.getId() + ",");
            sb.append("\"name\":\"" + escapeJson(shop.getName()) + "\",");
            sb.append("\"category\":\"" + escapeJson(shop.getCategory()) + "\",");
            sb.append("\"rating\":" + (shop.getRating() != null ? shop.getRating() : 0) + ",");
            sb.append("\"address\":\"" + escapeJson(shop.getAddress()) + "\",");
            sb.append("\"tags\":\"" + escapeJson(shop.getTags()) + "\"");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }

    private String buildChatPrompt(String userQuery, String city, String shopListJson) {
        return String.format(
            "你是本地生活推荐助手，可以推荐美食、酒店、电影、景点等各类本地生活服务。\n\n" +
            "用户需求：%s\n\n" +
            "可用%s的店铺/服务列表：%s\n\n" +
            "请按以下JSON格式返回推荐结果：\n" +
            "{\n" +
            "  \"reply\": \"对用户的友好回复\",\n" +
            "  \"scene\": \"识别到的场景类型，如：美食、酒店、电影、景点、综合等\",\n" +
            "  \"reasoning\": \"推荐理由的总结\",\n" +
            "  \"recommendations\": [\n" +
            "    {\n" +
            "      \"shopId\": 店铺ID,\n" +
            "      \"rank\": 1,\n" +
            "      \"reason\": \"为什么推荐这家\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "注意：\n" +
            "1. 根据用户意图推荐最合适的场景：美食、酒店住宿、电影观影、景点游玩等\n" +
            "2. 如果用户问的是天气、时间等与生活服务无关的问题，请礼貌引导用户询问本地生活服务相关需求\n" +
            "3. 最多推荐3家店铺/服务，按匹配度排序\n" +
            "4. 必须严格从提供的列表中选择\n" +
            "5. 推荐理由要具体说明为什么适合用户的需求\n" +
            "6. 只返回JSON，不要其他文字",
            city, userQuery, shopListJson
        );
    }

    private ChatRecommendationResult parseAiResponse(String aiResponse, String userQuery, List<ShopDTO> allShops) {
        try {
            String jsonStr = extractJson(aiResponse);
            if (jsonStr == null) {
                logger.warn("No JSON found in AI response: {}", aiResponse);
                return fallbackRecommendation(userQuery, "", allShops);
            }

            JsonNode root = objectMapper.readTree(jsonStr);

            String reply = root.path("reply").asText();
            String scene = root.path("scene").asText();

            ChatRecommendationResult result = new ChatRecommendationResult();
            result.setScene(scene);
            result.setReply(reply);

            JsonNode analysisNode = root.path("analysis");
            if (!analysisNode.isMissingNode()) {
                result.setScene(analysisNode.path("scene").asText());
                result.setPreferences(analysisNode.path("preferences").asText());
                result.setReasoning(analysisNode.path("reasoning").asText());
            }

            List<ShopRecommendation> recommendations = new ArrayList<>();
            JsonNode recsNode = root.path("recommendations");
            if (recsNode.isArray()) {
                for (JsonNode recNode : recsNode) {
                    Long shopId = recNode.path("shopId").asLong();
                    String reason = recNode.path("reason").asText();
                    int rank = recNode.path("rank").asInt(1);

                    ShopDTO shop = findShopById(allShops, shopId);
                    if (shop != null) {
                        ShopRecommendation rec = new ShopRecommendation();
                        rec.setShop(shop);
                        rec.setReason(reason);
                        rec.setRank(rank);
                        recommendations.add(rec);
                    }
                }
            }

            result.setRecommendations(recommendations);
            return result;

        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", aiResponse, e);
            return fallbackRecommendation(userQuery, "", allShops);
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    private ShopDTO findShopById(List<ShopDTO> shops, Long id) {
        return shops.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private ChatRecommendationResult fallbackRecommendation(String query, String city, List<ShopDTO> shops) {
        ChatRecommendationResult result = new ChatRecommendationResult();
        result.setRestaurantQuery(true);
        result.setReply("为您推荐以下热门餐厅：");
        result.setScene("综合推荐");
        result.setPreferences("热门店铺");

        List<ShopRecommendation> recommendations = shops.stream()
                .sorted(Comparator.comparing(s -> s.getRating() != null ? -s.getRating() : 0))
                .limit(3)
                .map(shop -> {
                    ShopRecommendation rec = new ShopRecommendation();
                    rec.setShop(shop);
                    rec.setReason("评分高，深受用户喜爱");
                    rec.setRank(1);
                    return rec;
                })
                .collect(Collectors.toList());

        result.setRecommendations(recommendations);
        return result;
    }

    public static class ChatRecommendationResult {
        private boolean isRestaurantQuery;
        private String reply;
        private String scene;
        private String preferences;
        private String reasoning;
        private List<ShopRecommendation> recommendations;
        private String errorMessage;

        public static ChatRecommendationResult noData(String message) {
            ChatRecommendationResult r = new ChatRecommendationResult();
            r.setRestaurantQuery(true);
            r.setReply(message);
            r.setRecommendations(Collections.emptyList());
            return r;
        }

        public static ChatRecommendationResult error(String message) {
            ChatRecommendationResult r = new ChatRecommendationResult();
            r.setRestaurantQuery(false);
            r.setErrorMessage(message);
            r.setRecommendations(Collections.emptyList());
            return r;
        }

        public boolean isRestaurantQuery() { return isRestaurantQuery; }
        public void setRestaurantQuery(boolean restaurantQuery) { isRestaurantQuery = restaurantQuery; }
        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
        public String getScene() { return scene; }
        public void setScene(String scene) { this.scene = scene; }
        public String getPreferences() { return preferences; }
        public void setPreferences(String preferences) { this.preferences = preferences; }
        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }
        public List<ShopRecommendation> getRecommendations() { return recommendations; }
        public void setRecommendations(List<ShopRecommendation> recommendations) { this.recommendations = recommendations; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class ShopRecommendation {
        private ShopDTO shop;
        private String reason;
        private int rank;

        public ShopDTO getShop() { return shop; }
        public void setShop(ShopDTO shop) { this.shop = shop; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
    }
}
