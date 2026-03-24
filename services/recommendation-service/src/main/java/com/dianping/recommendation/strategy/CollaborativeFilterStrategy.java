package com.dianping.recommendation.strategy;

import com.dianping.recommendation.algorithm.ItemCollaborativeFilter;
import com.dianping.recommendation.client.ShopClient;
import com.dianping.common.dto.ShopDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 协同过滤推荐策略
 * 基于ItemCF算法，利用用户购买历史推荐相似店铺
 */
@Component
public class CollaborativeFilterStrategy implements RecommendStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CollaborativeFilterStrategy.class);
    
    private final ItemCollaborativeFilter cfAlgorithm;
    private final ShopClient shopClient;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 用户购买历史缓存
    private static final String USER_HISTORY_KEY = "dp:rec:user_history:";
    
    public CollaborativeFilterStrategy(ItemCollaborativeFilter cfAlgorithm,
                                       ShopClient shopClient,
                                       RedisTemplate<String, Object> redisTemplate) {
        this.cfAlgorithm = cfAlgorithm;
        this.shopClient = shopClient;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public String getName() {
        return "collaborative_filter";
    }
    
    @Override
    public List<ShopDTO> recommend(Long userId, String city, Double longitude, Double latitude, Integer size) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 1. 获取用户购买历史
        List<Long> userHistory = getUserHistory(userId);
        if (userHistory.isEmpty()) {
            logger.debug("用户无购买历史, userId={}", userId);
            return Collections.emptyList();
        }
        
        // 2. 获取该城市候选店铺
        List<Long> candidateShops = getCandidateShops(city);
        if (candidateShops.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 3. 使用CF算法生成推荐
        List<Long> recommendedIds = cfAlgorithm.recommend(userId, userHistory, candidateShops, size);
        
        // 4. 获取店铺详情
        return fetchShopDetails(recommendedIds);
    }
    
    @Override
    public boolean isSupported(Long userId) {
        if (userId == null) {
            return false;
        }
        // 检查用户是否有购买历史
        List<Long> history = getUserHistory(userId);
        return history != null && !history.isEmpty();
    }
    
    /**
     * 获取用户购买历史
     */
    @SuppressWarnings("unchecked")
    private List<Long> getUserHistory(Long userId) {
        String key = USER_HISTORY_KEY + userId;
        List<Long> cached = (List<Long>) redisTemplate.opsForValue().get(key);
        return cached != null ? cached : Collections.emptyList();
    }
    
    /**
     * 获取候选店铺（该城市的所有店铺）
     */
    private List<Long> getCandidateShops(String city) {
        try {
            List<ShopDTO> shops = shopClient.listByCity(city);
            if (shops == null || shops.isEmpty()) {
                return Collections.emptyList();
            }
            return shops.stream()
                    .map(ShopDTO::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取候选店铺失败, city={}", city, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取店铺详情
     */
    private List<ShopDTO> fetchShopDetails(List<Long> shopIds) {
        if (shopIds == null || shopIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<ShopDTO> result = new ArrayList<>();
        for (Long shopId : shopIds) {
            try {
                ShopDTO shop = shopClient.getById(shopId);
                if (shop != null) {
                    result.add(shop);
                }
            } catch (Exception e) {
                logger.warn("获取店铺详情失败, shopId={}", shopId);
            }
        }
        return result;
    }
}
