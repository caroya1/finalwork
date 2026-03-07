package com.dianping.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.mapper.ShopMapper;
import com.dianping.common.dto.ShopSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private static final Logger log = LoggerFactory.getLogger(ShopService.class);
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
        Object cached = safeGet(cacheKey);
        if (cached instanceof Shop) {
            return (Shop) cached;
        }
        Shop shop = shopMapper.selectById(id);
        if (shop != null) {
            safeSet(cacheKey, shop, cacheTtlSeconds, TimeUnit.SECONDS);
        }
        return shop;
    }

    public Shop getPlainById(Long id) {
        if (id == null) {
            return null;
        }
        return shopMapper.selectById(id);
    }

    public ShopSummary getSummary(Long id) {
        Shop shop = getById(id);
        if (shop == null) {
            return null;
        }
        return new ShopSummary(
                shop.getId(),
                shop.getName(),
                shop.getCategory(),
                shop.getCity(),
                shop.getRating(),
                shop.getAddress()
        );
    }

    public List<ShopSummary> listSummaries(String city, String category) {
        List<Shop> shops = list(city, category);
        List<ShopSummary> result = new ArrayList<>();
        for (Shop shop : shops) {
            result.add(new ShopSummary(
                    shop.getId(),
                    shop.getName(),
                    shop.getCategory(),
                    shop.getCity(),
                    shop.getRating(),
                    shop.getAddress()
            ));
        }
        return result;
    }

    public Shop create(Shop shop) {
        shop.touchForCreate();
        shopMapper.insert(shop);
        invalidateCache(shop.getId(), shop.getCity(), shop.getCategory());
        return shop;
    }

    public List<Shop> list(String city, String category) {
        String cacheKey = buildListCacheKey(city, category);
        Object cached = safeGet(cacheKey);
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
            safeSet(cacheKey, shops, cacheTtlSeconds, TimeUnit.SECONDS);
        }
        return shops;
    }

    public List<Shop> search(String city, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String trimmedCity = city == null ? null : city.trim();
        String trimmedKeyword = keyword.trim();
        return shopMapper.search(trimmedCity, trimmedKeyword);
    }

    public void invalidateCache(Long shopId, String city, String category) {
        if (shopId != null) {
            safeDelete(SHOP_CACHE_PREFIX + shopId);
        }
        if (city != null && !city.trim().isEmpty()) {
            safeDelete(buildListCacheKey(city, null));
            if (category != null && !category.trim().isEmpty()) {
                List<String> categories = splitCategories(category);
                if (categories.size() <= 1) {
                    safeDelete(buildListCacheKey(city, category));
                } else {
                    for (String item : categories) {
                        safeDelete(buildListCacheKey(city, item));
                    }
                    safeDelete(buildListCacheKey(city, String.join(",", categories)));
                }
            }
        }
    }

    private Object safeGet(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RuntimeException ex) {
            log.warn("Redis get failed for key {}, fallback to database", key, ex);
            safeDelete(key);
            return null;
        }
    }

    private void safeSet(String key, Object value, long ttl, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, unit);
        } catch (RuntimeException ex) {
            log.warn("Redis set failed for key {}, skip cache write", key, ex);
        }
    }

    private void safeDelete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ignored) {
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
        String safeCity = StringUtils.hasText(city) ? city.trim() : "all";
        String safeCategory = StringUtils.hasText(category) ? category.trim() : "all";
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
