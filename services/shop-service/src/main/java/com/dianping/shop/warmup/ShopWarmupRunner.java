package com.dianping.shop.warmup;

import com.dianping.common.warmup.WarmupRunner;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.mapper.ShopMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ShopWarmupRunner extends WarmupRunner {
    
    private static final String SHOP_LIST_CACHE_PREFIX = "dp:shop:list:";
    private static final int WARMUP_LIMIT = 50;
    
    private final ShopMapper shopMapper;
    
    public ShopWarmupRunner(JdbcTemplate jdbcTemplate, 
                            RedisTemplate<String, Object> redisTemplate,
                            ShopMapper shopMapper) {
        super("shop-service", jdbcTemplate, redisTemplate);
        this.shopMapper = shopMapper;
    }
    
    @Override
    protected void warmupBusiness() {
        warmupHotShops();
    }
    
    private void warmupHotShops() {
        try {
            long start = System.currentTimeMillis();
            
            LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Shop::getRating)
                   .last("LIMIT " + WARMUP_LIMIT);
            List<Shop> hotShops = shopMapper.selectList(wrapper);
            
            if (hotShops != null && !hotShops.isEmpty()) {
                for (Shop shop : hotShops) {
                    String cacheKey = SHOP_LIST_CACHE_PREFIX + "hot:" + shop.getId();
                    redisTemplate.opsForValue().set(cacheKey, shop, 30, TimeUnit.MINUTES);
                }
                logger.info("[shop-service] 预热 {} 家热门店铺缓存，耗时 {} ms", 
                        hotShops.size(), System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            logger.warn("[shop-service] 热门店铺缓存预热失败: {}", e.getMessage());
        }
    }
}
