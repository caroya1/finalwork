package com.dianping.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dianping.user.entity.UserPrivacySettings;
import com.dianping.user.mapper.UserPrivacySettingsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户隐私设置服务
 */
@Service
public class UserPrivacyService extends ServiceImpl<UserPrivacySettingsMapper, UserPrivacySettings> {

    private static final Logger log = LoggerFactory.getLogger(UserPrivacyService.class);

    @Autowired
    private UserPrivacySettingsMapper privacyMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String PRIVACY_CACHE_PREFIX = "dp:privacy:";
    private static final long CACHE_TTL_HOURS = 1;

    /**
     * 获取或创建默认隐私设置
     */
    public UserPrivacySettings getOrCreate(Long userId) {
        // 1. 查缓存
        String cacheKey = PRIVACY_CACHE_PREFIX + userId;
        UserPrivacySettings settings = (UserPrivacySettings) redisTemplate.opsForValue().get(cacheKey);
        
        if (settings != null) {
            return settings;
        }
        
        // 2. 查数据库
        settings = privacyMapper.selectByUserId(userId);
        
        // 3. 不存在则创建默认设置
        if (settings == null) {
            settings = createDefaultSettings(userId);
        }
        
        // 4. 写入缓存
        redisTemplate.opsForValue().set(cacheKey, settings, CACHE_TTL_HOURS, TimeUnit.HOURS);
        
        return settings;
    }

    /**
     * 更新隐私设置
     */
    public void updateSettings(Long userId, UserPrivacySettings newSettings) {
        UserPrivacySettings existing = privacyMapper.selectByUserId(userId);
        
        if (existing == null) {
            // 新建
            newSettings.setUserId(userId);
            newSettings.setCreatedAt(LocalDateTime.now());
            newSettings.setUpdatedAt(LocalDateTime.now());
            privacyMapper.insert(newSettings);
        } else {
            // 更新
            newSettings.setId(existing.getId());
            newSettings.setUserId(userId);
            newSettings.setUpdatedAt(LocalDateTime.now());
            privacyMapper.updateById(newSettings);
        }
        
        // 清除缓存
        evictCache(userId);
        
        log.info("用户隐私设置已更新: userId={}", userId);
    }

    /**
     * 检查是否可查看帖子
     */
    public boolean canViewPosts(Long targetUserId, Long currentUserId) {
        if (targetUserId.equals(currentUserId)) {
            return true; // 自己可以看自己的
        }
        
        UserPrivacySettings settings = getOrCreate(targetUserId);
        return settings.getShowPosts() != null && settings.getShowPosts();
    }

    /**
     * 创建默认隐私设置
     */
    private UserPrivacySettings createDefaultSettings(Long userId) {
        UserPrivacySettings settings = new UserPrivacySettings();
        settings.setUserId(userId);
        settings.setShowPosts(true);      // 默认公开帖子
        settings.setShowFollowing(true);  // 默认公开关注列表
        settings.setShowFollowers(true);  // 默认公开粉丝列表
        settings.setShowPhone(false);     // 默认隐藏手机号
        settings.setShowEmail(false);     // 默认隐藏邮箱
        settings.setCreatedAt(LocalDateTime.now());
        settings.setUpdatedAt(LocalDateTime.now());
        
        privacyMapper.insert(settings);
        return settings;
    }

    /**
     * 清除缓存
     */
    public void evictCache(Long userId) {
        String cacheKey = PRIVACY_CACHE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }
}
