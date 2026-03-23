package com.dianping.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * 权限缓存服务
 * 管理权限校验结果的缓存
 */
@Service
@Slf4j
public class PermissionCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String PERMISSION_CACHE_PREFIX = "dp:permission:";

    /**
     * 清除用户所有权限缓存（角色变更时调用）
     */
    public void evictUserPermissions(Long userId) {
        String pattern = PERMISSION_CACHE_PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
            log.info("清除用户权限缓存: userId={}, keys={}", userId, keys.size());
        }
    }

    /**
     * 清除指定资源的权限缓存
     */
    public void evictResourcePermissions(String entityType, Long resourceId) {
        String pattern = PERMISSION_CACHE_PREFIX + "*:" + entityType + ":" + resourceId;
        Set<String> keys = redisTemplate.keys(pattern);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
            log.info("清除资源权限缓存: entityType={}, resourceId={}", entityType, resourceId);
        }
    }

    /**
     * 清除所有权限缓存（慎用）
     */
    public void evictAll() {
        String pattern = PERMISSION_CACHE_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
            log.info("清除所有权限缓存: keys={}", keys.size());
        }
    }
}
