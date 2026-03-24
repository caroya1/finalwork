package com.dianping.recommendation.strategy;

import com.dianping.recommendation.client.ShopClient;
import com.dianping.common.dto.ShopDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 混合推荐策略（默认策略）
 * 
 * 新用户：热门推荐(80%) + 附近推荐(20%)
 * 老用户：协同过滤(70%) + 热门推荐(30%)
 */
@Primary
@Component
public class HybridRecommendStrategy implements RecommendStrategy {

    private static final Logger logger = LoggerFactory.getLogger(HybridRecommendStrategy.class);
    
    private final CollaborativeFilterStrategy cfStrategy;
    private final HotRecommendStrategy hotStrategy;
    private final ShopClient shopClient;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 用户购买历史缓存Key
    private static final String USER_HISTORY_KEY = "dp:rec:user_history:";
    // 冷启动判断阈值（订单数<3认为是新用户）
    private static final int COLD_START_THRESHOLD = 3;
    // 推荐结果缓存
    private static final String RECOMMEND_CACHE_KEY = "dp:rec:user:";
    // 缓存时间5分钟
    private static final long CACHE_EXPIRE_MINUTES = 5;
    
    public HybridRecommendStrategy(CollaborativeFilterStrategy cfStrategy,
                                   HotRecommendStrategy hotStrategy,
                                   ShopClient shopClient,
                                   RedisTemplate<String, Object> redisTemplate) {
        this.cfStrategy = cfStrategy;
        this.hotStrategy = hotStrategy;
        this.shopClient = shopClient;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public String getName() {
        return "hybrid";
    }
    
    @Override
    public List<ShopDTO> recommend(Long userId, String city, Double longitude, Double latitude, Integer size) {
        // 1. 检查缓存
        String cacheKey = RECOMMEND_CACHE_KEY + userId + ":" + city;
        @SuppressWarnings("unchecked")
        List<ShopDTO> cached = (List<ShopDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            logger.debug("从缓存获取推荐结果, userId={}, size={}", userId, cached.size());
            return cached.stream().limit(size).collect(Collectors.toList());
        }
        
        // 2. 判断用户类型
        boolean isNewUser = isNewUser(userId);
        logger.info("生成推荐, userId={}, isNewUser={}, city={}", userId, isNewUser, city);
        
        List<ShopDTO> recommendations;
        if (isNewUser) {
            // 新用户：热门80% + 附近20%
            recommendations = recommendForNewUser(userId, city, longitude, latitude, size);
        } else {
            // 老用户：协同过滤70% + 热门30%
            recommendations = recommendForOldUser(userId, city, size);
        }
        
        // 3. 写入缓存
        if (!recommendations.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, recommendations, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        
        return recommendations.stream().limit(size).collect(Collectors.toList());
    }
    
    @Override
    public boolean isSupported(Long userId) {
        return true; // 混合策略对所有用户都支持
    }
    
    /**
     * 新用户推荐策略
     */
    private List<ShopDTO> recommendForNewUser(Long userId, String city, 
                                               Double longitude, Double latitude, Integer size) {
        int hotSize = (int) (size * 0.8);
        int nearbySize = size - hotSize;
        
        // 获取热门推荐
        List<ShopDTO> hotShops = hotStrategy.recommend(userId, city, longitude, latitude, hotSize);
        
        // 获取附近推荐
        List<ShopDTO> nearbyShops = recommendNearby(city, longitude, latitude, nearbySize, hotShops);
        
        // 合并结果（去重）
        Set<Long> seen = new HashSet<>();
        List<ShopDTO> result = new ArrayList<>();
        
        for (ShopDTO shop : hotShops) {
            if (seen.add(shop.getId())) {
                result.add(shop);
            }
        }
        
        for (ShopDTO shop : nearbyShops) {
            if (seen.add(shop.getId())) {
                result.add(shop);
            }
        }
        
        return result;
    }
    
    /**
     * 老用户推荐策略
     */
    private List<ShopDTO> recommendForOldUser(Long userId, String city, Integer size) {
        int cfSize = (int) (size * 0.7);
        int hotSize = size - cfSize;
        
        // 获取协同过滤推荐
        List<ShopDTO> cfShops = cfStrategy.recommend(userId, city, null, null, cfSize);
        
        // 获取热门推荐补充
        List<ShopDTO> hotShops = hotStrategy.recommend(userId, city, null, null, hotSize);
        
        // 合并结果（CF优先）
        Set<Long> seen = new HashSet<>();
        List<ShopDTO> result = new ArrayList<>();
        
        // 先加CF推荐
        for (ShopDTO shop : cfShops) {
            if (seen.add(shop.getId())) {
                result.add(shop);
            }
        }
        
        // 再加热门推荐补充
        for (ShopDTO shop : hotShops) {
            if (seen.add(shop.getId())) {
                result.add(shop);
            }
        }
        
        return result;
    }
    
    /**
     * 附近推荐
     */
    private List<ShopDTO> recommendNearby(String city, Double longitude, Double latitude, 
                                          Integer size, List<ShopDTO> excludeShops) {
        if (longitude == null || latitude == null) {
            return Collections.emptyList();
        }
        
        try {
            // 获取该城市所有店铺
            List<ShopDTO> allShops = shopClient.listByCity(city);
            if (allShops == null || allShops.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 排除已推荐的
            Set<Long> excludeIds = excludeShops.stream()
                    .map(ShopDTO::getId)
                    .collect(Collectors.toSet());
            
            // 计算距离并排序
            return allShops.stream()
                    .filter(shop -> !excludeIds.contains(shop.getId()))
                    .filter(shop -> shop.getLongitude() != null && shop.getLatitude() != null)
                    .map(shop -> {
                        double distance = calculateDistance(longitude, latitude, 
                                shop.getLongitude(), shop.getLatitude());
                        shop.setDistance(distance);
                        return shop;
                    })
                    .sorted(Comparator.comparing(ShopDTO::getDistance))
                    .limit(size)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            logger.error("获取附近店铺失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 判断是否是新用户
     */
    private boolean isNewUser(Long userId) {
        if (userId == null) {
            return true;
        }
        
        @SuppressWarnings("unchecked")
        List<Long> history = (List<Long>) redisTemplate.opsForValue().get(USER_HISTORY_KEY + userId);
        if (history == null || history.isEmpty()) {
            return true;
        }
        
        return history.size() < COLD_START_THRESHOLD;
    }
    
    /**
     * 计算两点间距离（单位：公里）
     */
    private double calculateDistance(Double lon1, Double lat1, Double lon2, Double lat2) {
        final double R = 6371; // 地球半径（公里）
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
