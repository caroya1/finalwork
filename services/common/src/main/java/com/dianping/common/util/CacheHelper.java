package com.dianping.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存工具类 - 提供缓存穿透、雪崩防护
 */
@Component
public class CacheHelper {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // 空值缓存时间（分钟）
    private static final long NULL_CACHE_MINUTES = 5;
    // 缓存击穿互斥锁前缀
    private static final String LOCK_PREFIX = "dp:cache:lock:";
    // 空值标记
    private static final String NULL_VALUE = "__NULL__";

    public CacheHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取缓存，如果不存在则加载（防缓存穿透）
     */
    @SuppressWarnings("unchecked")
    public <T> T getWithNullProtection(String key, Class<T> type, long timeout, TimeUnit unit, Supplier<T> loader) {
        Object value = redisTemplate.opsForValue().get(key);
        
        // 命中缓存
        if (value != null) {
            // 空值标记，返回null
            if (NULL_VALUE.equals(value)) {
                return null;
            }
            return (T) value;
        }
        
        // 缓存未命中，从数据库加载
        T result = loader.get();
        
        if (result != null) {
            // 缓存有效值
            redisTemplate.opsForValue().set(key, result, timeout, unit);
        } else {
            // 缓存空值，防止缓存穿透
            redisTemplate.opsForValue().set(key, NULL_VALUE, NULL_CACHE_MINUTES, TimeUnit.MINUTES);
        }
        
        return result;
    }

    /**
     * 获取缓存，使用互斥锁防止缓存击穿
     */
    @SuppressWarnings("unchecked")
    public <T> T getWithLock(String key, Class<T> type, long timeout, TimeUnit unit, Supplier<T> loader) {
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value != null) {
            if (NULL_VALUE.equals(value)) {
                return null;
            }
            return (T) value;
        }
        
        // 获取互斥锁
        String lockKey = LOCK_PREFIX + key;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(locked)) {
            try {
                // 双重检查
                value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    return NULL_VALUE.equals(value) ? null : (T) value;
                }
                
                // 加载数据
                T result = loader.get();
                
                if (result != null) {
                    redisTemplate.opsForValue().set(key, result, timeout, unit);
                } else {
                    redisTemplate.opsForValue().set(key, NULL_VALUE, NULL_CACHE_MINUTES, TimeUnit.MINUTES);
                }
                
                return result;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            // 未获取到锁，短暂等待后重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getWithLock(key, type, timeout, unit, loader);
        }
    }

    /**
     * 设置缓存，添加随机过期时间（防缓存雪崩）
     */
    public void setWithRandomExpire(String key, Object value, long baseTimeout, TimeUnit unit) {
        // 添加随机偏移量（0-10%）
        long offset = (long) (baseTimeout * 0.1 * Math.random());
        long finalTimeout = baseTimeout + offset;
        redisTemplate.opsForValue().set(key, value, finalTimeout, unit);
    }

    /**
     * 批量预热缓存
     */
    public <T> void preloadCache(String keyPrefix, java.util.List<T> dataList, 
                                  java.util.function.Function<T, String> keyExtractor,
                                  long timeout, TimeUnit unit) {
        for (T data : dataList) {
            String key = keyPrefix + keyExtractor.apply(data);
            setWithRandomExpire(key, data, timeout, unit);
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除缓存
     */
    public void deleteByPattern(String pattern) {
        java.util.Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 更新缓存（先更新数据库，再删除缓存）
     */
    public <T> T updateCache(String key, Supplier<T> updater) {
        T result = updater.get();
        delete(key);
        return result;
    }
}
