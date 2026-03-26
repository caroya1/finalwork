package com.dianping.ai.service;

import com.dianping.ai.strategy.AiRecommendStrategy;
import com.dianping.common.dto.RecommendRequest;
import com.dianping.common.dto.ShopDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AiRecommendationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AiRecommendationService.class);
    
    private final AiRecommendStrategy aiRecommendStrategy;
    
    public AiRecommendationService(AiRecommendStrategy aiRecommendStrategy) {
        this.aiRecommendStrategy = aiRecommendStrategy;
    }
    
    public List<ShopDTO> recommend(RecommendRequest request) {
        try {
            String query = request.getQuery();
            String city = request.getCity();
            Long userId = request.getUserId();
            
            logger.info("AI推荐请求: query={}, city={}, userId={}", query, city, userId);
            
            if (query == null || query.trim().isEmpty()) {
                logger.warn("推荐请求缺少query参数");
                return Collections.emptyList();
            }
            
            if (city == null || city.trim().isEmpty()) {
                logger.warn("推荐请求缺少city参数");
                return Collections.emptyList();
            }
            
            // 设置查询并执行推荐
            logger.info("调用AiRecommendStrategy.setQuery: {}", query);
            aiRecommendStrategy.setQuery(query);
            
            logger.info("调用AiRecommendStrategy.recommend...");
            List<ShopDTO> shops = aiRecommendStrategy.recommend(userId, city, null, null, 10);
            
            logger.info("AI推荐结果: 找到{}家店铺", shops.size());
            return shops;
            
        } catch (Exception e) {
            logger.error("AI推荐服务异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
