package com.dianping.common.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据分析服务 - 用户行为漏斗分析、推荐效果分析
 */
@Service
public class DataAnalysisService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String PAGE_VIEW_COUNT = "dp:analytics:pv:";
    private static final String UV_COUNT = "dp:analytics:uv:";
    private static final String EVENT_COUNT = "dp:analytics:event:";
    private static final String FUNNEL_PREFIX = "dp:analytics:funnel:";
    private static final String RECOMMEND_PREFIX = "dp:analytics:recommend:";
    
    public DataAnalysisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void recordPageView(String pageName, Long userId) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String pvKey = PAGE_VIEW_COUNT + date + ":" + pageName;
        redisTemplate.opsForValue().increment(pvKey);
        
        if (userId != null) {
            String uvKey = UV_COUNT + date + ":" + pageName;
            redisTemplate.opsForSet().add(uvKey, userId);
        }
    }
    
    public Long getPagePV(String pageName) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String key = PAGE_VIEW_COUNT + date + ":" + pageName;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }
    
    public Long getPageUV(String pageName) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String key = UV_COUNT + date + ":" + pageName;
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0L;
    }
    
    public void createFunnel(String funnelName, List<String> steps) {
        String key = FUNNEL_PREFIX + funnelName + ":steps";
        redisTemplate.opsForList().rightPushAll(key, steps.toArray());
    }
    
    public void recordFunnelConversion(String funnelName, String step, Long userId) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String key = FUNNEL_PREFIX + funnelName + ":" + date + ":" + step;
        redisTemplate.opsForSet().add(key, userId);
    }
    
    public Map<String, Object> getFunnelConversion(String funnelName, List<String> steps) {
        Map<String, Object> result = new HashMap<>();
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        long previousCount = Long.MAX_VALUE;
        
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            String key = FUNNEL_PREFIX + funnelName + ":" + date + ":" + step;
            Long count = redisTemplate.opsForSet().size(key);
            count = count != null ? count : 0L;
            
            Map<String, Object> stepData = new HashMap<>();
            stepData.put("step", step);
            stepData.put("count", count);
            
            if (previousCount != Long.MAX_VALUE && previousCount > 0) {
                double conversionRate = (double) count / previousCount * 100;
                stepData.put("conversionRate", String.format("%.2f%%", conversionRate));
            }
            
            result.put("step" + i, stepData);
            previousCount = count;
        }
        
        return result;
    }
    
    public void recordRecommendClick(Long userId, Long shopId, String position, String algorithm) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        String exposeKey = RECOMMEND_PREFIX + "expose:" + date + ":" + algorithm + ":" + position;
        redisTemplate.opsForSet().add(exposeKey, userId + ":" + shopId);
        
        if (userId != null) {
            String clickKey = RECOMMEND_PREFIX + "click:" + date + ":" + algorithm + ":" + position;
            redisTemplate.opsForSet().add(clickKey, userId + ":" + shopId);
        }
    }
    
    public Map<String, Object> getRecommendStats(String algorithm, String position) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        
        String exposeKey = RECOMMEND_PREFIX + "expose:" + date + ":" + algorithm + ":" + position;
        String clickKey = RECOMMEND_PREFIX + "click:" + date + ":" + algorithm + ":" + position;
        
        Long exposeCount = redisTemplate.opsForSet().size(exposeKey);
        Long clickCount = redisTemplate.opsForSet().size(clickKey);
        
        exposeCount = exposeCount != null ? exposeCount : 0L;
        clickCount = clickCount != null ? clickCount : 0L;
        
        double ctr = exposeCount > 0 ? (double) clickCount / exposeCount * 100 : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("algorithm", algorithm);
        stats.put("position", position);
        stats.put("exposeCount", exposeCount);
        stats.put("clickCount", clickCount);
        stats.put("ctr", String.format("%.2f%%", ctr));
        
        return stats;
    }
}
