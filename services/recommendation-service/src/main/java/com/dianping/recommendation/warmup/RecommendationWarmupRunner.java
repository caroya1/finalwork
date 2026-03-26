package com.dianping.recommendation.warmup;

import com.dianping.common.dto.ShopSummary;
import com.dianping.common.port.ShopPort;
import com.dianping.common.warmup.WarmupRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RecommendationWarmupRunner extends WarmupRunner {
    
    private static final String HOT_SHOPS_CACHE_KEY = "dp:rec:hot:default";
    private static final String[] DEFAULT_CITIES = {"北京", "上海", "广州", "深圳"};
    
    private final ShopPort shopPort;
    
    public RecommendationWarmupRunner(JdbcTemplate jdbcTemplate,
                                      RedisTemplate<String, Object> redisTemplate,
                                      ShopPort shopPort) {
        super("recommendation-service", jdbcTemplate, redisTemplate);
        this.shopPort = shopPort;
    }
    
    @Override
    protected void warmupBusiness() {
        // 暂时禁用预热，测试是否能正常启动
        logger.info("[recommendation-service] 预热已禁用，用于调试");
        // warmupFeignClient();
        // warmupHotRecommendations();
    }
    
    private void warmupFeignClient() {
        try {
            long start = System.currentTimeMillis();
            shopPort.listSummaries(null, null);
            logger.info("[recommendation-service] Feign客户端预热完成，耗时 {} ms", 
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.warn("[recommendation-service] Feign客户端预热失败: {}", e.getMessage());
        }
    }
    
    private void warmupHotRecommendations() {
        try {
            long start = System.currentTimeMillis();
            int warmedCount = 0;
            
            for (String city : DEFAULT_CITIES) {
                try {
                    List<ShopSummary> shops = shopPort.listSummaries(city, null);
                    if (shops != null && !shops.isEmpty()) {
                        String cacheKey = "dp:rec:hot:" + city;
                        redisTemplate.opsForValue().set(cacheKey, shops, 30, TimeUnit.MINUTES);
                        warmedCount += shops.size();
                    }
                } catch (Exception e) {
                    logger.debug("[recommendation-service] 城市 {} 预热失败: {}", city, e.getMessage());
                }
            }
            
            logger.info("[recommendation-service] 预热 {} 个城市的热门推荐，共 {} 家店铺，耗时 {} ms",
                    DEFAULT_CITIES.length, warmedCount, System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.warn("[recommendation-service] 热门推荐缓存预热失败: {}", e.getMessage());
        }
    }
}
