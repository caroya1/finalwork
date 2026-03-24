package com.dianping.recommendation.task;

import com.dianping.recommendation.algorithm.ItemCollaborativeFilter;
import com.dianping.recommendation.mapper.RecommendationLogMapper;
import com.dianping.recommendation.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 推荐系统定时任务
 * 每8分钟执行一次，预计算推荐数据
 */
@Component
public class RecommendPrecomputeTask {

    private static final Logger logger = LoggerFactory.getLogger(RecommendPrecomputeTask.class);
    
    private final ItemCollaborativeFilter cfAlgorithm;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RecommendationLogMapper logMapper;
    
    // 缓存Key
    private static final String USER_HISTORY_KEY = "dp:rec:user_history:";
    private static final String LAST_COMPUTE_TIME = "dp:rec:last_compute";
    
    public RecommendPrecomputeTask(ItemCollaborativeFilter cfAlgorithm,
                                   RedisTemplate<String, Object> redisTemplate,
                                   RecommendationLogMapper logMapper) {
        this.cfAlgorithm = cfAlgorithm;
        this.redisTemplate = redisTemplate;
        this.logMapper = logMapper;
    }
    
    /**
     * 每8分钟执行一次推荐数据预计算
     */
    @Scheduled(fixedRate = 8 * 60 * 1000)
    public void precompute() {
        logger.info("开始执行推荐数据预计算任务...");
        
        try {
            // 1. 加载用户购买历史到Redis
            loadUserHistory();
            
            // 2. 计算店铺相似度矩阵
            computeShopSimilarity();
            
            // 3. 清理过期缓存
            cleanExpiredCache();
            
            // 4. 记录计算时间
            redisTemplate.opsForValue().set(LAST_COMPUTE_TIME, new Date().toString());
            
            logger.info("推荐数据预计算任务完成");
        } catch (Exception e) {
            logger.error("推荐数据预计算任务失败", e);
        }
    }
    
    /**
     * 加载用户购买历史
     */
    private void loadUserHistory() {
        logger.info("开始加载用户购买历史...");
        
        try {
            // 从订单表获取用户购买数据
            // 这里简化处理，实际应该从order-service查询
            // 模拟数据加载
            Map<Long, List<Long>> userShopMap = getUserShopMapFromOrders();
            
            // 写入Redis
            for (Map.Entry<Long, List<Long>> entry : userShopMap.entrySet()) {
                String key = USER_HISTORY_KEY + entry.getKey();
                redisTemplate.opsForValue().set(key, entry.getValue(), 10, TimeUnit.MINUTES);
            }
            
            logger.info("用户购买历史加载完成, 用户数={}", userShopMap.size());
        } catch (Exception e) {
            logger.error("加载用户购买历史失败", e);
        }
    }
    
    /**
     * 计算店铺相似度矩阵
     */
    private void computeShopSimilarity() {
        logger.info("开始计算店铺相似度矩阵...");
        
        try {
            // 获取用户-店铺购买映射
            Map<Long, List<Long>> userShopMap = getUserShopMapFromOrders();
            
            if (userShopMap.isEmpty()) {
                logger.warn("无用户购买数据，跳过相似度计算");
                return;
            }
            
            // 调用协同过滤算法计算相似度
            cfAlgorithm.computeSimilarityMatrix(userShopMap);
            
            logger.info("店铺相似度矩阵计算完成");
        } catch (Exception e) {
            logger.error("计算店铺相似度矩阵失败", e);
        }
    }
    
    /**
     * 清理过期缓存
     */
    private void cleanExpiredCache() {
        logger.info("开始清理过期缓存...");
        
        try {
            // 获取所有推荐相关的key
            Set<String> keys = redisTemplate.keys("dp:rec:*");
            if (keys != null && !keys.isEmpty()) {
                int count = 0;
                for (String key : keys) {
                    // 检查TTL，如果小于60秒则删除
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl != null && ttl < 60) {
                        redisTemplate.delete(key);
                        count++;
                    }
                }
                logger.info("清理过期缓存完成, 删除={}个", count);
            }
        } catch (Exception e) {
            logger.error("清理过期缓存失败", e);
        }
    }
    
    /**
     * 从订单获取用户-店铺映射
     * 简化实现，实际应该调用order-service接口
     */
    private Map<Long, List<Long>> getUserShopMapFromOrders() {
        Map<Long, List<Long>> userShopMap = new HashMap<>();
        
        // 这里应该从数据库或order-service获取真实数据
        // 由于侵入性要求，暂时返回空数据
        // 实际使用时需要：
        // 1. 通过Feign调用order-service获取订单数据
        // 2. 或者查询推荐日志表获取用户行为
        
        // 示例：从推荐日志表获取点击数据作为替代
        try {
            // 查询最近的用户行为日志
            // List<RecommendationLog> logs = logMapper.selectRecentLogs();
            // 处理日志构建用户-店铺映射
        } catch (Exception e) {
            logger.warn("获取用户购买数据失败", e);
        }
        
        return userShopMap;
    }
}
