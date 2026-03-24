package com.dianping.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 内部Token服务
 * 用于服务间调用的认证
 */
@Service
@ConditionalOnBean(StringRedisTemplate.class)
public class InternalTokenService {

    private static final Logger log = LoggerFactory.getLogger(InternalTokenService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String TOKEN_KEY = "dp:internal:token";
    private static final long TOKEN_EXPIRE_DAYS = 7; // 7天有效期

    @PostConstruct
    public void init() {
        // 服务启动时生成新的Token
        String token = generateToken();
        log.info("内部Token已生成，有效期7天: {}", token.substring(0, 8) + "...");
    }

    /**
     * 生成新的内部Token
     */
    public String generateToken() {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
            TOKEN_KEY, 
            token, 
            TOKEN_EXPIRE_DAYS, 
            TimeUnit.DAYS
        );
        return token;
    }

    /**
     * 验证内部Token
     */
    public boolean validate(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        String storedToken = redisTemplate.opsForValue().get(TOKEN_KEY);
        return token.equals(storedToken);
    }

    /**
     * 获取当前Token（用于Feign调用）
     */
    public String getCurrentToken() {
        return redisTemplate.opsForValue().get(TOKEN_KEY);
    }

    /**
     * 刷新Token（手动触发）
     */
    public String refreshToken() {
        log.info("手动刷新内部Token");
        return generateToken();
    }
}
