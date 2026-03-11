package com.dianping.coupon.controller;

import com.dianping.common.exception.BusinessException;
import com.dianping.coupon.entity.Coupon;
import com.dianping.coupon.entity.CouponPurchase;
import com.dianping.coupon.mapper.CouponMapper;
import com.dianping.coupon.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 秒杀压力测试接口
 * 用于测试秒杀功能在高并发场景下的表现
 */
@RestController
@RequestMapping("/api/stress-test")
public class SeckillTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(SeckillTestController.class);
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private CouponMapper couponMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String SECKILL_STOCK_KEY_PREFIX = "dp:seckill:stock:";
    private static final String SECKILL_USER_KEY_PREFIX = "dp:seckill:user:";
    
    /**
     * 执行秒杀压力测试
     * 
     * @param userCount 并发用户数，默认1000
     * @param stockCount 库存数量，默认50
     * @param couponId 优惠券ID
     * @return 测试结果
     */
    @PostMapping("/seckill-stress")
    public Map<String, Object> runStressTest(
            @RequestParam(name = "userCount", defaultValue = "1000") int userCount,
            @RequestParam(name = "stockCount", defaultValue = "50") int stockCount,
            @RequestParam(name = "couponId", defaultValue = "999") Long couponId) {
        
        logger.info("开始秒杀压力测试: 用户数={}, 库存={}, 优惠券ID={}", userCount, stockCount, couponId);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 准备测试数据
            prepareTestData(couponId, stockCount);
            
            // 2. 创建并发线程池
            ExecutorService executor = Executors.newFixedThreadPool(200);
            CountDownLatch latch = new CountDownLatch(userCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger stockErrorCount = new AtomicInteger(0);
            AtomicInteger duplicateErrorCount = new AtomicInteger(0);
            AtomicInteger otherErrorCount = new AtomicInteger(0);
            ConcurrentHashMap<Long, Integer> userPurchaseCount = new ConcurrentHashMap<>();
            List<String> errorList = Collections.synchronizedList(new ArrayList<>());
            
            // 3. 提交并发任务
            for (int i = 0; i < userCount; i++) {
                final int userIndex = i;
                executor.submit(() -> {
                    Long userId = (long) (10000 + userIndex);
                    try {
                        CouponPurchase purchase = couponService.purchase(couponId, userId);
                        
                        if (purchase != null && "processing".equals(purchase.getStatus())) {
                            successCount.incrementAndGet();
                            userPurchaseCount.merge(userId, 1, Integer::sum);
                        }
                    } catch (BusinessException e) {
                        String msg = e.getMessage();
                        if (msg != null && (msg.contains("sold out") || msg.contains("售罄"))) {
                            stockErrorCount.incrementAndGet();
                        } else if (msg != null && (msg.contains("already purchased") || msg.contains("已购买"))) {
                            duplicateErrorCount.incrementAndGet();
                        } else {
                            otherErrorCount.incrementAndGet();
                            errorList.add("用户" + userId + ":" + msg);
                        }
                    } catch (Exception e) {
                        otherErrorCount.incrementAndGet();
                        errorList.add("用户" + userId + "异常:" + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // 4. 等待所有任务完成
            latch.await();
            executor.shutdown();
            
            // 5. 等待 MQ 消费完成（最多等待 10 秒）
            logger.info("等待 MQ 消费完成...");
            int waitCount = 0;
            while (waitCount < 20) {
                Thread.sleep(500);
                String currentRedisStock = stringRedisTemplate.opsForValue().get(SECKILL_STOCK_KEY_PREFIX + couponId);
                Coupon currentCoupon = couponMapper.selectById(couponId);
                int currentDbStock = currentCoupon != null ? currentCoupon.getRemainingStock() : -1;
                
                if (currentRedisStock != null && Integer.parseInt(currentRedisStock) == currentDbStock) {
                    logger.info("MQ 消费完成，Redis={}, DB={}", currentRedisStock, currentDbStock);
                    break;
                }
                waitCount++;
            }
            if (waitCount >= 20) {
                logger.warn("等待 MQ 消费超时");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 6. 验证结果
            Coupon finalCoupon = couponMapper.selectById(couponId);
            int finalStock = finalCoupon != null ? finalCoupon.getRemainingStock() : -1;
            String redisStock = stringRedisTemplate.opsForValue().get(SECKILL_STOCK_KEY_PREFIX + couponId);
            
            // 检查重复购买
            int duplicateUsers = 0;
            for (Map.Entry<Long, Integer> entry : userPurchaseCount.entrySet()) {
                if (entry.getValue() > 1) {
                    duplicateUsers++;
                }
            }
            
            // 组装结果
            result.put("success", true);
            result.put("duration", duration);
            result.put("tps", String.format("%.2f", userCount * 1000.0 / duration));
            result.put("concurrentUsers", userCount);
            result.put("stockCount", stockCount);
            result.put("successCount", successCount.get());
            result.put("stockErrorCount", stockErrorCount.get());
            result.put("duplicateErrorCount", duplicateErrorCount.get());
            result.put("otherErrorCount", otherErrorCount.get());
            result.put("finalStock", finalStock);
            result.put("redisStock", redisStock);
            result.put("duplicateUsers", duplicateUsers);
            
            // 验证结果
            List<String> validations = new ArrayList<>();
            validations.add(successCount.get() == stockCount ? "✅ 成功购买数正确" : "❌ 成功购买数错误:" + successCount.get() + "!=" + stockCount);
            validations.add(finalStock == 0 ? "✅ 最终库存为0" : "❌ 最终库存错误:" + finalStock);
            validations.add(duplicateUsers == 0 ? "✅ 无重复购买" : "❌ 重复购买用户:" + duplicateUsers);
            validations.add(redisStock != null && Integer.parseInt(redisStock) == finalStock ? "✅ Redis和数据库一致" : "❌ Redis和数据库不一致");
            result.put("validations", validations);
            result.put("allPassed", successCount.get() == stockCount && finalStock == 0 && duplicateUsers == 0);
            
            if (!errorList.isEmpty()) {
                result.put("sampleErrors", errorList.stream().limit(10).collect(Collectors.toList()));
            }
            
            logger.info("压力测试完成: {}ms, 成功:{}", duration, successCount.get());
            
        } catch (Exception e) {
            logger.error("压力测试异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    private void prepareTestData(Long couponId, int stockCount) {
        logger.info("准备测试数据: couponId={}, stock={}", couponId, stockCount);
        
        // 清理Redis
        stringRedisTemplate.delete(SECKILL_STOCK_KEY_PREFIX + couponId);
        stringRedisTemplate.delete("dp:seckill:coupon:" + couponId);
        Set<String> userKeys = stringRedisTemplate.keys(SECKILL_USER_KEY_PREFIX + couponId + ":*");
        if (userKeys != null && !userKeys.isEmpty()) {
            stringRedisTemplate.delete(userKeys);
        }
        
        // 清理旧数据
        couponMapper.deleteById(couponId);
        
        // 创建新优惠券
        Coupon coupon = new Coupon();
        coupon.setId(couponId);
        coupon.setTitle("压力测试秒杀券");
        coupon.setType("seckill");
        coupon.setTotalStock(stockCount);
        coupon.setRemainingStock(stockCount);
        coupon.setPrice(new BigDecimal("0.01"));
        coupon.setStartTime(LocalDateTime.now().minusMinutes(1));
        coupon.setEndTime(LocalDateTime.now().plusHours(1));
        coupon.setShopId(1L);
        coupon.setDiscountAmount(new BigDecimal("5.00"));
        coupon.touchForCreate();
        couponMapper.insert(coupon);
        
        // 预热Redis
        stringRedisTemplate.opsForValue().set(
            SECKILL_STOCK_KEY_PREFIX + couponId, 
            String.valueOf(stockCount), 
            3600, 
            java.util.concurrent.TimeUnit.SECONDS
        );
        
        logger.info("测试数据准备完成");
    }
}
