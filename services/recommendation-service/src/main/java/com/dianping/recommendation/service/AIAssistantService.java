package com.dianping.recommendation.service;

import com.dianping.common.dto.GenerateReasonRequest;
import com.dianping.common.dto.GenerateReasonResponse;
import com.dianping.common.dto.RecommendRequest;
import com.dianping.common.dto.ShopDTO;
import com.dianping.common.port.AiPort;
import com.dianping.common.port.ShopPort;
import com.dianping.recommendation.dto.AIRecommendedShopDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AIAssistantService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);
    private static final int MAX_RECOMMENDATIONS = 3;
    
    private final AiPort aiPort;
    private final ShopPort shopPort;
    
    public AIAssistantService(AiPort aiPort, ShopPort shopPort) {
        this.aiPort = aiPort;
        this.shopPort = shopPort;
    }
    
    public List<AIRecommendedShopDTO> recommendForAssistant(Long userId, String city, String query) {
        try {
            // 直接调用AI服务获取推荐
            RecommendRequest request = new RecommendRequest(query, city, userId);
            List<ShopDTO> shops = aiPort.recommend(request);
            
            if (shops == null || shops.isEmpty()) {
                logger.warn("AI推荐未找到店铺，降级为热门店铺");
                shops = getFallbackShops(city);
            }
            
            List<ShopDTO> limitedShops = shops.size() > MAX_RECOMMENDATIONS 
                ? shops.subList(0, MAX_RECOMMENDATIONS) 
                : shops;
            
            return convertAndEnrichWithAI(limitedShops, query);
            
        } catch (Exception e) {
            logger.error("AI助手推荐失败", e);
            return Collections.emptyList();
        }
    }
    
    private List<ShopDTO> getFallbackShops(String city) {
        try {
            return shopPort.listSummaries(city, null)
                .stream()
                .map(summary -> {
                    ShopDTO dto = new ShopDTO();
                    dto.setId(summary.getId());
                    dto.setName(summary.getName());
                    dto.setCategory(summary.getCategory());
                    dto.setCity(summary.getCity());
                    dto.setRating(summary.getRating());
                    return dto;
                })
                .limit(MAX_RECOMMENDATIONS)
                .toList();
        } catch (Exception e) {
            logger.error("获取降级店铺失败", e);
            return Collections.emptyList();
        }
    }
    
    private List<AIRecommendedShopDTO> convertAndEnrichWithAI(List<ShopDTO> shops, String query) {
        List<AIRecommendedShopDTO> result = new ArrayList<>();
        
        for (int i = 0; i < shops.size(); i++) {
            ShopDTO shop = shops.get(i);
            AIRecommendedShopDTO dto = new AIRecommendedShopDTO();
            
            dto.setId(shop.getId());
            dto.setName(shop.getName());
            dto.setCategory(shop.getCategory());
            dto.setCity(shop.getCity());
            dto.setRating(shop.getRating());
            dto.setHotScore(shop.getHotScore());
            dto.setImages(shop.getImages());
            dto.setAddress(shop.getAddress());
            dto.setTags(shop.getTags());
            
            enrichWithAIGeneratedContent(dto, shop, query, i);
            
            result.add(dto);
        }
        
        return result;
    }
    
    private void enrichWithAIGeneratedContent(AIRecommendedShopDTO dto, ShopDTO shop, String query, int rank) {
        try {
            GenerateReasonRequest request = new GenerateReasonRequest();
            request.setShopName(shop.getName());
            request.setCategory(shop.getCategory());
            request.setRating(shop.getRating());
            request.setTags(shop.getTags());
            request.setUserQuery(query);
            request.setRank(rank);
            
            GenerateReasonResponse aiResponse = aiPort.generateReason(request);
            
            if (aiResponse != null) {
                if (aiResponse.getRecommendReason() != null && !aiResponse.getRecommendReason().isEmpty()) {
                    dto.setRecommendReason(aiResponse.getRecommendReason());
                } else {
                    dto.setRecommendReason(generateFallbackReason(shop, rank));
                }
                
                if (aiResponse.getHighlights() != null && !aiResponse.getHighlights().isEmpty()) {
                    dto.setHighlights(aiResponse.getHighlights());
                } else {
                    dto.setHighlights(generateFallbackHighlights(shop));
                }
            } else {
                setFallbackContent(dto, shop, rank);
            }
        } catch (Exception e) {
            logger.warn("AI生成推荐理由失败，使用默认内容, shopId={}", shop.getId(), e);
            setFallbackContent(dto, shop, rank);
        }
    }
    
    private void setFallbackContent(AIRecommendedShopDTO dto, ShopDTO shop, int rank) {
        dto.setRecommendReason(generateFallbackReason(shop, rank));
        dto.setHighlights(generateFallbackHighlights(shop));
    }
    
    private String generateFallbackReason(ShopDTO shop, int rank) {
        String[] reasons = {"最符合您的需求，强烈推荐", "性价比之选，值得一试", "品质保证，不错的选择"};
        String reason = reasons[Math.min(rank, reasons.length - 1)];
        
        if (shop.getRating() != null && shop.getRating() >= 4.5) {
            reason += "，评分" + String.format("%.1f", shop.getRating()) + "分";
        }
        
        return reason;
    }
    
    private List<String> generateFallbackHighlights(ShopDTO shop) {
        List<String> highlights = new ArrayList<>();
        
        String category = shop.getCategory();
        if (category != null) {
            if (category.contains("火锅")) {
                highlights.add("锅底正宗，食材新鲜");
            } else if (category.contains("日料")) {
                highlights.add("食材新鲜，制作精良");
            } else if (category.contains("西餐")) {
                highlights.add("环境优雅，菜品精致");
            } else {
                highlights.add("口味地道，深受好评");
            }
        }
        
        if (shop.getRating() != null && shop.getRating() >= 4.5) {
            highlights.add("评分优秀，口碑极佳");
        } else {
            highlights.add("服务周到，环境舒适");
        }
        
        return highlights.subList(0, Math.min(highlights.size(), 2));
    }
}
