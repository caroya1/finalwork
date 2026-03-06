package com.dianping.recommendation.service;

import com.dianping.recommendation.dto.RecommendationRequest;
import com.dianping.recommendation.entity.RecommendationLog;
import com.dianping.recommendation.mapper.RecommendationLogMapper;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.service.ShopService;
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

@Service
public class RecommendationService {
    private static final String CACHE_PREFIX = "dp:rec:";

    private final ShopService shopService;
    private final RecommendationLogMapper logMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long cacheTtlSeconds;
    private final Executor appTaskExecutor;

    public RecommendationService(ShopService shopService,
                                 RecommendationLogMapper logMapper,
                                 RedisTemplate<String, Object> redisTemplate,
                                 @Value("${app.recommendation.cache-ttl-seconds:300}") long cacheTtlSeconds,
                                 @Qualifier("appTaskExecutor") Executor appTaskExecutor) {
        this.shopService = shopService;
        this.logMapper = logMapper;
        this.redisTemplate = redisTemplate;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.appTaskExecutor = appTaskExecutor;
    }

    public List<Shop> recommend(RecommendationRequest request) {
        String cacheKey = buildCacheKey(request);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List) {
            return castList(cached);
        }

        List<Shop> shops = shopService.list(request.getCity(), null);
        Collections.shuffle(shops);
        List<Shop> result = shops.size() > 10 ? shops.subList(0, 10) : shops;

        if (!CollectionUtils.isEmpty(result)) {
            redisTemplate.opsForValue().set(cacheKey, result, cacheTtlSeconds, TimeUnit.SECONDS);
        }

        appTaskExecutor.execute(() -> {
            for (Shop shop : result) {
                RecommendationLog log = new RecommendationLog();
                log.setUserId(request.getUserId());
                log.setShopId(shop.getId());
                log.setScene(request.getScene() == null ? "default" : request.getScene());
                log.setAction("recommend");
                log.touchForCreate();
                logMapper.insert(log);
            }
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Shop> castList(Object cached) {
        if (cached instanceof List) {
            return (List<Shop>) cached;
        }
        return new ArrayList<>();
    }

    private String buildCacheKey(RecommendationRequest request) {
        String scene = request.getScene() == null ? "default" : request.getScene();
        return CACHE_PREFIX + request.getUserId() + ":" + request.getCity() + ":" + scene;
    }
}
