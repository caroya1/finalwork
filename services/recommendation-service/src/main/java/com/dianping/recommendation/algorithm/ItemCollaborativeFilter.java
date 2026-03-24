package com.dianping.recommendation.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Item-based 协同过滤算法
 * 基于店铺共现计算相似度
 */
@Component
public class ItemCollaborativeFilter {

    private static final Logger logger = LoggerFactory.getLogger(ItemCollaborativeFilter.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 相似度缓存Key
    private static final String SIMILARITY_KEY = "dp:rec:similarity:";
    // 相似度计算阈值（最少共同用户数）
    private static final int MIN_COMMON_USERS = 2;
    // 每个店铺保留的相似店铺数量
    private static final int MAX_SIMILAR_SHOPS = 20;
    
    public ItemCollaborativeFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 计算店铺相似度矩阵
     * 
     * @param userShopMap 用户-店铺购买映射 Map<userId, List<shopId>>
     */
    public void computeSimilarityMatrix(Map<Long, List<Long>> userShopMap) {
        logger.info("开始计算店铺相似度矩阵, 用户数={}", userShopMap.size());
        
        // 1. 构建店铺-用户倒排表
        Map<Long, Set<Long>> shopUserMap = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : userShopMap.entrySet()) {
            Long userId = entry.getKey();
            for (Long shopId : entry.getValue()) {
                shopUserMap.computeIfAbsent(shopId, k -> new HashSet<>()).add(userId);
            }
        }
        
        logger.info("店铺-用户倒排表构建完成, 店铺数={}", shopUserMap.size());
        
        // 2. 计算店铺间相似度
        List<Long> shopIds = new ArrayList<>(shopUserMap.keySet());
        Map<Long, List<ShopSimilarity>> similarityMatrix = new HashMap<>();
        
        for (int i = 0; i < shopIds.size(); i++) {
            Long shopA = shopIds.get(i);
            Set<Long> usersA = shopUserMap.get(shopA);
            
            List<ShopSimilarity> similarities = new ArrayList<>();
            
            for (int j = 0; j < shopIds.size(); j++) {
                if (i == j) continue;
                
                Long shopB = shopIds.get(j);
                Set<Long> usersB = shopUserMap.get(shopB);
                
                // 计算Jaccard相似度
                double similarity = calculateJaccardSimilarity(usersA, usersB);
                
                if (similarity > 0 && usersA.size() >= MIN_COMMON_USERS) {
                    similarities.add(new ShopSimilarity(shopB, similarity));
                }
            }
            
            // 按相似度排序，保留TopN
            similarities.sort(Comparator.comparing(ShopSimilarity::getScore).reversed());
            if (similarities.size() > MAX_SIMILAR_SHOPS) {
                similarities = similarities.subList(0, MAX_SIMILAR_SHOPS);
            }
            
            similarityMatrix.put(shopA, similarities);
        }
        
        // 3. 写入Redis缓存
        saveSimilarityMatrix(similarityMatrix);
        
        logger.info("店铺相似度矩阵计算完成");
    }
    
    /**
     * 为用户生成推荐
     * 
     * @param userId          用户ID
     * @param userHistory     用户历史购买店铺
     * @param candidateShops  候选店铺池
     * @param size            推荐数量
     * @return 推荐店铺ID列表
     */
    public List<Long> recommend(Long userId, List<Long> userHistory, 
                                List<Long> candidateShops, int size) {
        if (userHistory == null || userHistory.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 1. 获取相似店铺的加权分数
        Map<Long, Double> shopScores = new HashMap<>();
        
        for (Long historyShopId : userHistory) {
            List<ShopSimilarity> similarShops = getSimilarShops(historyShopId);
            
            for (ShopSimilarity similar : similarShops) {
                Long shopId = similar.getShopId();
                
                // 排除已购买过的
                if (userHistory.contains(shopId)) {
                    continue;
                }
                
                // 累加相似度分数
                shopScores.merge(shopId, similar.getScore(), Double::sum);
            }
        }
        
        // 2. 按分数排序
        return shopScores.entrySet().stream()
                .filter(entry -> candidateShops.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(size)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * 计算Jaccard相似度
     */
    private double calculateJaccardSimilarity(Set<Long> setA, Set<Long> setB) {
        if (setA == null || setB == null || setA.isEmpty() || setB.isEmpty()) {
            return 0.0;
        }
        
        // 计算交集
        Set<Long> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        
        // 计算并集
        Set<Long> union = new HashSet<>(setA);
        union.addAll(setB);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * 获取相似店铺
     */
    @SuppressWarnings("unchecked")
    private List<ShopSimilarity> getSimilarShops(Long shopId) {
        String key = SIMILARITY_KEY + shopId;
        List<ShopSimilarity> cached = (List<ShopSimilarity>) redisTemplate.opsForValue().get(key);
        return cached != null ? cached : Collections.emptyList();
    }
    
    /**
     * 保存相似度矩阵到Redis
     */
    private void saveSimilarityMatrix(Map<Long, List<ShopSimilarity>> matrix) {
        for (Map.Entry<Long, List<ShopSimilarity>> entry : matrix.entrySet()) {
            String key = SIMILARITY_KEY + entry.getKey();
            redisTemplate.opsForValue().set(key, entry.getValue(), 10, TimeUnit.MINUTES);
        }
    }
    
    /**
     * 店铺相似度对象
     */
    public static class ShopSimilarity {
        private Long shopId;
        private Double score;
        
        public ShopSimilarity(Long shopId, Double score) {
            this.shopId = shopId;
            this.score = score;
        }
        
        public Long getShopId() { return shopId; }
        public void setShopId(Long shopId) { this.shopId = shopId; }
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }
}
