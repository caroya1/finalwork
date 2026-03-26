package com.dianping.recommendation.strategy;

import com.dianping.common.dto.RecommendRequest;
import com.dianping.common.dto.ShopDTO;
import com.dianping.common.port.AiPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI推荐策略
 * 通过调用AI服务获取智能推荐结果
 */
@Component
public class AiRecommendStrategy implements RecommendStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AiRecommendStrategy.class);
    
    private static final String AI_SHOPS_KEY = "dp:rec:ai:";
    private static final long CACHE_EXPIRE_MINUTES = 5;
    private static final int MAX_RESULTS = 20;
    
    private final AiPort aiPort;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HotRecommendStrategy hotRecommendStrategy;
    
    @Value("${app.recommendation.ai.enabled:true}")
    private boolean aiEnabled;
    
    @Autowired(required = false)
    public AiRecommendStrategy(
            AiPort aiPort,
            RedisTemplate<String, Object> redisTemplate,
            HotRecommendStrategy hotRecommendStrategy) {
        this.aiPort = aiPort;
        this.redisTemplate = redisTemplate;
        this.hotRecommendStrategy = hotRecommendStrategy;
    }
    
    @Override
    public String getName() {
        return "ai";
    }
    
    @Override
    public List<ShopDTO> recommend(Long userId, String city, Double longitude, Double latitude, Integer size) {
        if (!aiEnabled) {
            logger.debug("AI recommendation is disabled");
            return Collections.emptyList();
        }
        
        String cacheKey = AI_SHOPS_KEY + city + ":" + userId;
        int resultSize = size != null ? Math.min(size, MAX_RESULTS) : MAX_RESULTS;
        
        // 1. 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<ShopDTO> cachedShops = (List<ShopDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedShops != null && !cachedShops.isEmpty()) {
            logger.debug("从缓存获取AI推荐，city={}, userId={}, size={}", city, userId, cachedShops.size());
            return cachedShops.stream().limit(resultSize).collect(Collectors.toList());
        }
        
        // 2. 调用AI服务获取推荐
        List<ShopDTO> shops = fetchAiRecommendation(city, userId, resultSize);
        
        // 3. 写入缓存
        if (!shops.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, shops, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        
        return shops;
    }
    
    @Override
    public boolean isSupported(Long userId) {
        // AI推荐对所有用户都支持
        return aiEnabled;
    }
    
    /**
     * 获取AI推荐结果
     */
    private List<ShopDTO> fetchAiRecommendation(String city, Long userId, Integer size) {
        try {
            RecommendRequest request = new RecommendRequest(null, city, userId);
            List<ShopDTO> shops = aiPort.recommend(request);
            
            if (shops != null && !shops.isEmpty()) {
                logger.info("AI推荐成功，city={}, userId={}, count={}", city, userId, shops.size());
                return shops.stream().limit(size).collect(Collectors.toList());
            }
            
            logger.debug("AI推荐返回空结果，尝试使用热门推荐作为后备");
            return getFallbackShops(city, size);
            
        } catch (Exception e) {
            logger.error("AI推荐调用失败，city={}, userId={}, 错误: {}", city, userId, e.getMessage());
            return getFallbackShops(city, size);
        }
    }
    
    /**
     * 后备方案：使用热门推荐
     */
    private List<ShopDTO> getFallbackShops(String city, Integer size) {
        try {
            if (hotRecommendStrategy != null) {
                return hotRecommendStrategy.recommend(null, city, null, null, size);
            }
        } catch (Exception e) {
            logger.error("热门推荐后备方案失败，city={}", city, e);
        }
        return Collections.emptyList();
    }
    
    /**
     * 检查AI服务是否可用
     */
    public boolean isAiServiceAvailable() {
        if (!aiEnabled) {
            return false;
        }
        try {
            // 简单测试调用
            aiPort.recommend(new RecommendRequest("", "", 0L));
            return true;
        } catch (Exception e) {
            logger.warn("AI服务不可用: {}", e.getMessage());
            return false;
        }
    }
}
