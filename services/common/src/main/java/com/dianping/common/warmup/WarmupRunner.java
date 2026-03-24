package com.dianping.common.warmup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 服务启动预热器
 * 用于解决冷启动首次请求慢的问题
 * 
 * 预热内容：
 * 1. 数据库连接池 - 执行简单查询建立连接
 * 2. Redis 连接 - 执行 PING 命令建立连接
 * 3. 可被子类扩展添加业务预热逻辑
 */
public abstract class WarmupRunner implements ApplicationRunner {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected final JdbcTemplate jdbcTemplate;
    protected final RedisTemplate<String, Object> redisTemplate;
    protected final String serviceName;
    
    public WarmupRunner(String serviceName, JdbcTemplate jdbcTemplate, RedisTemplate<String, Object> redisTemplate) {
        this.serviceName = serviceName;
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public void run(ApplicationArguments args) {
        long startTime = System.currentTimeMillis();
        logger.info("[{}] 开始启动预热...", serviceName);
        
        try {
            // 1. 预热数据库连接池
            warmupDatabase();
            
            // 2. 预热 Redis 连接
            warmupRedis();
            
            // 3. 子类扩展的业务预热
            warmupBusiness();
            
            long elapsed = System.currentTimeMillis() - startTime;
            logger.info("[{}] 预热完成，耗时 {} ms", serviceName, elapsed);
        } catch (Exception e) {
            logger.warn("[{}] 预热过程中出现异常（不影响服务启动）: {}", serviceName, e.getMessage());
        }
    }
    
    /**
     * 预热数据库连接池
     * 执行简单查询，触发连接池初始化
     */
    protected void warmupDatabase() {
        if (jdbcTemplate == null) {
            return;
        }
        try {
            long start = System.currentTimeMillis();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            logger.debug("[{}] 数据库连接预热完成，耗时 {} ms", serviceName, System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.warn("[{}] 数据库连接预热失败: {}", serviceName, e.getMessage());
        }
    }
    
    /**
     * 预热 Redis 连接
     */
    protected void warmupRedis() {
        if (redisTemplate == null) {
            return;
        }
        try {
            long start = System.currentTimeMillis();
            String testKey = "warmup:test:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "warmup", 1, TimeUnit.SECONDS);
            redisTemplate.delete(testKey);
            logger.debug("[{}] Redis连接预热完成，耗时 {} ms", serviceName, System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.warn("[{}] Redis连接预热失败: {}", serviceName, e.getMessage());
        }
    }
    
    /**
     * 业务预热 - 子类实现
     * 例如：预热热门数据缓存、预热 Feign 客户端等
     */
    protected abstract void warmupBusiness();
}
