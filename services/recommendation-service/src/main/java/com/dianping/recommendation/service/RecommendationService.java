package com.dianping.recommendation.service;

import com.dianping.recommendation.dto.RecommendationRequest;
import com.dianping.recommendation.entity.RecommendationLog;
import com.dianping.recommendation.mapper.RecommendationLogMapper;
import com.dianping.recommendation.strategy.HybridRecommendStrategy;
import com.dianping.recommendation.strategy.RecommendStrategy;
import com.dianping.common.dto.ShopSummary;
import com.dianping.common.port.ShopPort;
import com.dianping.common.dto.ShopDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推荐服务
 * 基于策略模式，支持多种推荐算法
 */
@Service
public class RecommendationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private static final String CACHE_PREFIX = "dp:rec:";

    private final ShopPort shopPort;
    private final RecommendationLogMapper logMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long cacheTtlSeconds;
    private final Executor appTaskExecutor;
    private final HybridRecommendStrategy recommendStrategy;

    public RecommendationService(ShopPort shopPort,
                                  RecommendationLogMapper logMapper,
                                  RedisTemplate<String, Object> redisTemplate,
                                  @Value("${app.recommendation.cache-ttl-seconds:300}") long cacheTtlSeconds,
                                  @Qualifier("appTaskExecutor") Executor appTaskExecutor,
                                  HybridRecommendStrategy recommendStrategy) {
        this.shopPort = shopPort;
        this.logMapper = logMapper;
        this.redisTemplate = redisTemplate;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.appTaskExecutor = appTaskExecutor;
        this.recommendStrategy = recommendStrategy;
    }

    /**
     * 获取推荐店铺列表
     * 
     * @param request 推荐请求参数
     * @return 推荐店铺列表
     */
    public List<ShopSummary> recommend(RecommendationRequest request) {
        String cacheKey = buildCacheKey(request);
        
        // 1. 尝试从缓存获取
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List) {
            List<ShopSummary> result = castList(cached);
            logger.debug("从缓存获取推荐结果, userId={}, size={}", request.getUserId(), result.size());
            return result;
        }

        // 2. 使用推荐策略生成推荐
        List<ShopDTO> recommendedShops;
        try {
            recommendedShops = recommendStrategy.recommend(
                    request.getUserId(),
                    request.getCity(),
                    request.getLongitude(),
                    request.getLatitude(),
                    10
            );
        } catch (Exception e) {
            logger.error("推荐策略执行失败, userId={}, 降级为随机推荐", request.getUserId(), e);
            recommendedShops = fallbackRecommend(request.getCity());
        }

        // 3. 转换为ShopSummary
        List<ShopSummary> result = convertToShopSummary(recommendedShops);
        
        // 4. 写入缓存
        if (!CollectionUtils.isEmpty(result)) {
            redisTemplate.opsForValue().set(cacheKey, result, cacheTtlSeconds, TimeUnit.SECONDS);
        }

        // 5. 异步记录推荐日志
        appTaskExecutor.execute(() -> {
            try {
                recordRecommendationLogs(request, result);
            } catch (Exception e) {
                logger.error("记录推荐日志失败", e);
            }
        });
        
        return result;
    }

    /**
     * 降级推荐（当策略失败时使用）
     * 简单随机打乱
     */
    private List<ShopDTO> fallbackRecommend(String city) {
        try {
            // 通过shopPort获取店铺列表
            List<ShopSummary> shops = shopPort.listSummaries(city, null);
            if (CollectionUtils.isEmpty(shops)) {
                return Collections.emptyList();
            }
            
            // 转换为ShopDTO并随机打乱
            Collections.shuffle(shops);
            List<ShopSummary> selected = shops.size() > 10 ? shops.subList(0, 10) : shops;
            
            // 简单转换
            return selected.stream()
                    .map(this::convertSummaryToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("降级推荐也失败了", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * ShopSummary转换为ShopDTO
     */
    private ShopDTO convertSummaryToDto(ShopSummary summary) {
        ShopDTO dto = new ShopDTO();
        dto.setId(summary.getId());
        dto.setName(summary.getName());
        dto.setRating(summary.getRating());
        dto.setCity(summary.getCity());
        dto.setCategory(summary.getCategory());
        return dto;
    }
    
    /**
     * ShopDTO转换为ShopSummary
     */
    private List<ShopSummary> convertToShopSummary(List<ShopDTO> shops) {
        if (CollectionUtils.isEmpty(shops)) {
            return Collections.emptyList();
        }
        
        return shops.stream()
                .map(shop -> {
                    ShopSummary summary = new ShopSummary();
                    summary.setId(shop.getId());
                    summary.setName(shop.getName());
                    summary.setRating(shop.getRating());
                    summary.setCity(shop.getCity());
                    summary.setCategory(shop.getCategory());
                    return summary;
                })
                .collect(Collectors.toList());
    }

    /**
     * 记录推荐日志
     */
    private void recordRecommendationLogs(RecommendationRequest request, List<ShopSummary> result) {
        for (ShopSummary shop : result) {
            try {
                RecommendationLog log = new RecommendationLog();
                log.setUserId(request.getUserId());
                log.setShopId(shop.getId());
                log.setScene(request.getScene() == null ? "default" : request.getScene());
                log.setAction("recommend");
                log.touchForCreate();
                logMapper.insert(log);
            } catch (Exception e) {
                logger.warn("记录单条推荐日志失败, shopId={}", shop.getId());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<ShopSummary> castList(Object cached) {
        if (cached instanceof List) {
            return (List<ShopSummary>) cached;
        }
        return new ArrayList<>();
    }

    private String buildCacheKey(RecommendationRequest request) {
        String scene = request.getScene() == null ? "default" : request.getScene();
        return CACHE_PREFIX + request.getUserId() + ":" + request.getCity() + ":" + scene;
    }
}
