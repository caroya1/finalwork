package com.dianping.recommendation.strategy;

import com.dianping.recommendation.dto.RecommendationRequest;
import com.dianping.recommendation.client.ShopClient;
import com.dianping.common.dto.ShopDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 热门推荐策略
 * 基于店铺评分、月销量、优惠券热度计算
 */
@Component
public class HotRecommendStrategy implements RecommendStrategy {

    private static final Logger logger = LoggerFactory.getLogger(HotRecommendStrategy.class);
    
    private final ShopClient shopClient;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 缓存Key前缀
    private static final String HOT_SHOPS_KEY = "dp:rec:hot:";
    // 缓存过期时间（8分钟）
    private static final long CACHE_EXPIRE_MINUTES = 8;
    
    public HotRecommendStrategy(ShopClient shopClient, RedisTemplate<String, Object> redisTemplate) {
        this.shopClient = shopClient;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public String getName() {
        return "hot";
    }
    
    @Override
    public List<ShopDTO> recommend(Long userId, String city, Double longitude, Double latitude, Integer size) {
        String cacheKey = HOT_SHOPS_KEY + city;
        
        // 1. 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<ShopDTO> cachedShops = (List<ShopDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedShops != null && !cachedShops.isEmpty()) {
            logger.debug("从缓存获取热门推荐，city={}, size={}", city, cachedShops.size());
            return cachedShops.stream().limit(size).collect(Collectors.toList());
        }
        
        // 2. 从服务获取并计算热度
        List<ShopDTO> shops = fetchAndCalculateHotShops(city);
        
        // 3. 写入缓存
        if (!shops.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, shops, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        
        return shops.stream().limit(size).collect(Collectors.toList());
    }
    
    @Override
    public boolean isSupported(Long userId) {
        // 热门推荐对所有用户都支持
        return true;
    }
    
    /**
     * 获取并计算热门店铺
     */
    private List<ShopDTO> fetchAndCalculateHotShops(String city) {
        try {
            // 从shop服务获取该城市所有店铺
            List<ShopDTO> allShops = shopClient.listByCity(city);
            if (allShops == null || allShops.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 计算热度分并排序
            return allShops.stream()
                    .map(this::calculateHotScore)
                    .sorted(Comparator.comparing(ShopDTO::getHotScore).reversed())
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("获取热门店铺失败, city={}", city, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 计算店铺热度分
     * 热度分 = 评分 * 0.6 + 基础分 * 0.4
     * (由于目前没有销量数据，主要依赖评分)
     */
    private ShopDTO calculateHotScore(ShopDTO shop) {
        double score = 0.0;
        
        // 评分部分 (0-5分，权重60%)
        if (shop.getRating() != null) {
            score += shop.getRating() * 0.6;
        }
        
        // 基础分 (权重40%，新店给一定机会)
        score += 2.5 * 0.4;
        
        // 设置热度分
        shop.setHotScore(score);
        
        return shop;
    }
}
