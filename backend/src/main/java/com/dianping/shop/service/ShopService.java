package com.dianping.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.mapper.ShopMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private static final String SHOP_LIST_CACHE_PREFIX = "dp:shop:list:";
    private static final String SHOP_CACHE_PREFIX = "dp:shop:";

    private final ShopMapper shopMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long cacheTtlSeconds;

    public ShopService(ShopMapper shopMapper,
                       RedisTemplate<String, Object> redisTemplate,
                       @Value("${app.shop.cache-ttl-seconds:300}") long cacheTtlSeconds) {
        this.shopMapper = shopMapper;
        this.redisTemplate = redisTemplate;
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public Shop getById(Long id) {
        if (id == null) {
            return null;
        }
        String cacheKey = SHOP_CACHE_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Shop) {
            return (Shop) cached;
        }
        Shop shop = shopMapper.selectById(id);
        if (shop != null) {
            redisTemplate.opsForValue().set(cacheKey, shop, cacheTtlSeconds, TimeUnit.SECONDS);
        }
        return shop;
    }

    public Shop create(Shop shop) {
        shop.touchForCreate();
        shopMapper.insert(shop);
        invalidateCache(shop.getId(), shop.getCity(), shop.getCategory());
        return shop;
    }

    public List<Shop> list(String city, String category) {
        String cacheKey = buildListCacheKey(city, category);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List) {
            return castList(cached);
        }
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        if (city != null && !city.trim().isEmpty()) {
            wrapper.eq(Shop::getCity, city.trim());
        }
        if (category != null && !category.trim().isEmpty()) {
            List<String> categoryList = Arrays.stream(category.split(","))
                    .map(String::trim)
                    .filter(item -> !item.isEmpty())
                    .collect(Collectors.toList());
            if (categoryList.size() == 1) {
                wrapper.eq(Shop::getCategory, categoryList.get(0));
            } else if (!categoryList.isEmpty()) {
                wrapper.in(Shop::getCategory, categoryList);
            }
        }
        wrapper.orderByDesc(Shop::getRating).orderByDesc(Shop::getCreatedAt);
        List<Shop> shops = shopMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(shops)) {
            redisTemplate.opsForValue().set(cacheKey, shops, cacheTtlSeconds, TimeUnit.SECONDS);
        }
        return shops;
    }

    public void invalidateCache(Long shopId, String city, String category) {
        if (shopId != null) {
            redisTemplate.delete(SHOP_CACHE_PREFIX + shopId);
        }
        if (city != null && !city.trim().isEmpty()) {
            redisTemplate.delete(buildListCacheKey(city, null));
            if (category != null && !category.trim().isEmpty()) {
                List<String> categories = splitCategories(category);
                if (categories.size() <= 1) {
                    redisTemplate.delete(buildListCacheKey(city, category));
                } else {
                    for (String item : categories) {
                        redisTemplate.delete(buildListCacheKey(city, item));
                    }
                    redisTemplate.delete(buildListCacheKey(city, String.join(",", categories)));
                }
            }
        }
    }

    private List<String> splitCategories(String category) {
        if (category == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(category.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String buildListCacheKey(String city, String category) {
        String safeCity = city == null ? "all" : city.trim();
        String safeCategory = category == null ? "all" : category.trim();
        return SHOP_LIST_CACHE_PREFIX + safeCity + ":" + safeCategory;
    }

    @SuppressWarnings("unchecked")
    private List<Shop> castList(Object cached) {
        if (cached instanceof List) {
            return (List<Shop>) cached;
        }
        return new ArrayList<>();
    }
}
