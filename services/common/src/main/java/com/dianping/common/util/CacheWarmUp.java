package com.dianping.common.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存预热工具
 */
@Component
public class CacheWarmUp {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheHelper cacheHelper;

    public CacheWarmUp(RedisTemplate<String, Object> redisTemplate, CacheHelper cacheHelper) {
        this.redisTemplate = redisTemplate;
        this.cacheHelper = cacheHelper;
    }

    /**
     * 预热单个缓存
     */
    public <T> void warmUp(String key, T data, long timeout, TimeUnit unit) {
        cacheHelper.setWithRandomExpire(key, data, timeout, unit);
    }

    /**
     * 批量预热缓存
     */
    public <T> int warmUpBatch(String keyPrefix, List<T> dataList,
                                Function<T, String> keyExtractor,
                                long timeout, TimeUnit unit) {
        int count = 0;
        for (T data : dataList) {
            try {
                String key = keyPrefix + keyExtractor.apply(data);
                cacheHelper.setWithRandomExpire(key, data, timeout, unit);
                count++;
            } catch (Exception e) {
                // 记录日志但继续处理
                System.err.println("预热缓存失败: " + e.getMessage());
            }
        }
        return count;
    }

    /**
     * 预热热门店铺缓存
     */
    public <T> void warmUpHotShops(List<T> hotShops, Function<T, Long> idExtractor) {
        warmUpBatch("shop:", hotShops, 
                shop -> String.valueOf(idExtractor.apply(shop)),
                60, TimeUnit.MINUTES);
    }

    /**
     * 预热用户信息缓存
     */
    public <T> void warmUpActiveUsers(List<T> activeUsers, Function<T, Long> idExtractor) {
        warmUpBatch("user:", activeUsers,
                user -> String.valueOf(idExtractor.apply(user)),
                120, TimeUnit.MINUTES);
    }

    /**
     * 清除指定模式的缓存
     */
    public long clearCache(String pattern) {
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            return keys.size();
        }
        return 0;
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats(String pattern) {
        var keys = redisTemplate.keys(pattern);
        long count = keys != null ? keys.size() : 0;
        
        // 计算内存占用（估算）
        long estimatedMemory = 0;
        if (keys != null) {
            for (String key : keys) {
                Long size = redisTemplate.execute(
                        connection -> connection.stringCommands().strLen(key.getBytes()),
                        true
                );
                if (size != null) {
                    estimatedMemory += size;
                }
            }
        }
        
        return new CacheStats(count, estimatedMemory);
    }

    public static class CacheStats {
        private final long keyCount;
        private final long estimatedMemoryBytes;

        public CacheStats(long keyCount, long estimatedMemoryBytes) {
            this.keyCount = keyCount;
            this.estimatedMemoryBytes = estimatedMemoryBytes;
        }

        public long getKeyCount() {
            return keyCount;
        }

        public long getEstimatedMemoryBytes() {
            return estimatedMemoryBytes;
        }

        public String getEstimatedMemoryMB() {
            return String.format("%.2f MB", estimatedMemoryBytes / (1024.0 * 1024.0));
        }

        @Override
        public String toString() {
            return String.format("CacheStats{keys=%d, memory=%s}", 
                    keyCount, getEstimatedMemoryMB());
        }
    }
}
