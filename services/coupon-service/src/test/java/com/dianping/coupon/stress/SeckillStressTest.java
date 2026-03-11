package com.dianping.coupon.stress;

import com.dianping.common.exception.BusinessException;
import com.dianping.coupon.entity.Coupon;
import com.dianping.coupon.entity.CouponPurchase;
import com.dianping.coupon.mapper.CouponMapper;
import com.dianping.coupon.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 秒杀压力测试
 * 测试场景：1000个并发用户抢购50个库存的秒杀券
 * 
 * 预期目标：
 * 1. 只有50个用户能成功购买
 * 2. 每个用户最多购买1次（幂等性）
 * 3. 库存不超卖
 * 4. 最终库存为0
 * 5. 无异常错误
 */
@Component
public class SeckillStressTest implements CommandLineRunner {
    
    private static final String SECKILL_STOCK_KEY_PREFIX = "dp:seckill:stock:";
    private static final String SECKILL_COUPON_KEY_PREFIX = "dp:seckill:coupon:";
    private static final String SECKILL_USER_KEY_PREFIX = "dp:seckill:user:";
    
    private static final Logger logger = LoggerFactory.getLogger(SeckillStressTest.class);
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private CouponMapper couponMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    // 测试参数
    private static final int CONCURRENT_USERS = 1000;  // 并发用户数
    private static final int STOCK_COUNT = 50;          // 库存数量
    private static final Long TEST_COUPON_ID = 999L;    // 测试优惠券ID
    
    public void runTest() throws InterruptedException {
        logger.info("===========================================");
        logger.info("开始秒杀压力测试");
        logger.info("并发用户数: {}", CONCURRENT_USERS);
        logger.info("库存数量: {}", STOCK_COUNT);
        logger.info("===========================================\n");
        
        // 1. 准备测试数据
        prepareTestData();
        
        // 2. 创建并发线程池
        ExecutorService executor = Executors.newFixedThreadPool(200);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger stockErrorCount = new AtomicInteger(0);
        AtomicInteger duplicateErrorCount = new AtomicInteger(0);
        ConcurrentHashMap<Long, Integer> userPurchaseCount = new ConcurrentHashMap<>();
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        
        long startTime = System.currentTimeMillis();
        
        // 3. 提交并发任务
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userIndex = i;
            executor.submit(() -> {
                try {
                    Long userId = (long) (1000 + userIndex); // 用户ID从1000开始
                    
                    try {
                        // 调用秒杀接口
                        CouponPurchase purchase = couponService.purchase(TEST_COUPON_ID, userId);
                        
                        if (purchase != null && "processing".equals(purchase.getStatus())) {
                            successCount.incrementAndGet();
                            userPurchaseCount.merge(userId, 1, Integer::sum);
                            logger.debug("用户 {} 抢购成功", userId);
                        } else if (purchase != null && "paid".equals(purchase.getStatus())) {
                            // 已购买过（幂等）
                            duplicateErrorCount.incrementAndGet();
                        }
                    } catch (BusinessException e) {
                        String msg = e.getMessage();
                        if (msg != null && msg.contains("sold out")) {
                            stockErrorCount.incrementAndGet();
                            logger.debug("用户 {} 抢购失败：库存不足", userId);
                        } else if (msg != null && msg.contains("already purchased")) {
                            duplicateErrorCount.incrementAndGet();
                            logger.debug("用户 {} 抢购失败：已购买", userId);
                        } else {
                            failCount.incrementAndGet();
                            errors.add("用户" + userId + "失败：" + msg);
                            logger.debug("用户 {} 抢购失败：{}", userId, msg);
                        }
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        errors.add("用户" + userId + "异常：" + e.getMessage());
                        logger.error("用户 {} 发生异常", userId, e);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 4. 等待所有任务完成
        latch.await();
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 5. 验证结果
        validateResults(successCount, failCount, stockErrorCount, duplicateErrorCount, 
                        userPurchaseCount, errors, duration);
    }
    
    private void prepareTestData() {
        logger.info("准备测试数据...");
        
        // 清理旧的测试数据
        stringRedisTemplate.delete(SECKILL_STOCK_KEY_PREFIX + TEST_COUPON_ID);
        stringRedisTemplate.delete(SECKILL_COUPON_KEY_PREFIX + TEST_COUPON_ID);
        
        // 清理该优惠券的所有用户记录
        Set<String> keys = stringRedisTemplate.keys(SECKILL_USER_KEY_PREFIX + TEST_COUPON_ID + ":*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        
        // 创建测试优惠券
        Coupon coupon = new Coupon();
        coupon.setId(TEST_COUPON_ID);
        coupon.setTitle("秒杀测试券");
        coupon.setType("seckill");
        coupon.setTotalStock(STOCK_COUNT);
        coupon.setRemainingStock(STOCK_COUNT);
        coupon.setPrice(new BigDecimal("1.00"));
        coupon.setStartTime(LocalDateTime.now().minusMinutes(1));
        coupon.setEndTime(LocalDateTime.now().plusHours(1));
        coupon.setShopId(1L);
        coupon.touchForCreate();
        
        // 先删除旧数据
        couponMapper.deleteById(TEST_COUPON_ID);
        // 插入新数据
        couponMapper.insert(coupon);
        
        // 缓存到Redis
        long ttlSeconds = 3600; // 1小时
        stringRedisTemplate.opsForValue().set(
            SECKILL_STOCK_KEY_PREFIX + TEST_COUPON_ID, 
            String.valueOf(STOCK_COUNT), 
            ttlSeconds, 
            TimeUnit.SECONDS
        );
        
        logger.info("测试数据准备完成，优惠券ID: {}，库存: {}", TEST_COUPON_ID, STOCK_COUNT);
    }
    
    private void validateResults(AtomicInteger successCount, AtomicInteger failCount,
                                AtomicInteger stockErrorCount, AtomicInteger duplicateErrorCount,
                                ConcurrentHashMap<Long, Integer> userPurchaseCount,
                                List<String> errors, long duration) {
        logger.info("\n===========================================");
        logger.info("压力测试完成");
        logger.info("===========================================");
        logger.info("总耗时: {} ms ({} s)", duration, duration / 1000.0);
        logger.info("成功购买: {}", successCount.get());
        logger.info("库存不足: {}", stockErrorCount.get());
        logger.info("重复购买: {}", duplicateErrorCount.get());
        logger.info("其他失败: {}", failCount.get());
        logger.info("总请求数: {}", CONCURRENT_USERS);
        logger.info("===========================================\n");
        
        // 验证数据库
        Coupon finalCoupon = couponMapper.selectById(TEST_COUPON_ID);
        int finalStock = finalCoupon != null ? finalCoupon.getRemainingStock() : -1;
        logger.info("数据库最终库存: {}", finalStock);
        
        // 验证Redis
        String redisStock = stringRedisTemplate.opsForValue().get(SECKILL_STOCK_KEY_PREFIX + TEST_COUPON_ID);
        logger.info("Redis最终库存: {}", redisStock);
        
        // 检查是否有重复购买的用户
        int duplicateUsers = 0;
        for (Map.Entry<Long, Integer> entry : userPurchaseCount.entrySet()) {
            if (entry.getValue() > 1) {
                duplicateUsers++;
                logger.error("用户 {} 购买了 {} 次！", entry.getKey(), entry.getValue());
            }
        }
        
        logger.info("\n===========================================");
        logger.info("测试结果验证");
        logger.info("===========================================");
        
        boolean allPass = true;
        
        // 验证1：成功购买数应该等于库存数
        if (successCount.get() == STOCK_COUNT) {
            logger.info("✅ 成功购买数正确: {} == {}", successCount.get(), STOCK_COUNT);
        } else {
            logger.error("❌ 成功购买数错误: {} != {}", successCount.get(), STOCK_COUNT);
            allPass = false;
        }
        
        // 验证2：最终库存应该为0
        if (finalStock == 0) {
            logger.info("✅ 最终库存正确: 0");
        } else {
            logger.error("❌ 最终库存错误: {} != 0", finalStock);
            allPass = false;
        }
        
        // 验证3：无重复购买
        if (duplicateUsers == 0) {
            logger.info("✅ 无重复购买用户");
        } else {
            logger.error("❌ 发现 {} 个重复购买用户", duplicateUsers);
            allPass = false;
        }
        
        // 验证4：Redis和数据库一致
        if (redisStock != null && Integer.parseInt(redisStock) == finalStock) {
            logger.info("✅ Redis和数据库库存一致");
        } else {
            logger.error("❌ Redis({})和数据库({})库存不一致", redisStock, finalStock);
            allPass = false;
        }
        
        // 验证5：错误数合理
        int totalProcessed = successCount.get() + stockErrorCount.get() + duplicateErrorCount.get() + failCount.get();
        if (totalProcessed == CONCURRENT_USERS) {
            logger.info("✅ 所有请求都被处理");
        } else {
            logger.error("❌ 请求处理不完整: {}/{}", totalProcessed, CONCURRENT_USERS);
            allPass = false;
        }
        
        // 验证6：TPS（每秒事务数）
        double tps = CONCURRENT_USERS * 1000.0 / duration;
        logger.info("TPS: {:.2f}", tps);
        
        logger.info("===========================================");
        if (allPass) {
            logger.info("🎉 所有测试通过！");
        } else {
            logger.error("💥 测试未通过，请检查代码");
            if (!errors.isEmpty()) {
                logger.error("错误详情（前10条）：");
                errors.stream().limit(10).forEach(e -> logger.error("  - {}", e));
            }
        }
        logger.info("===========================================\n");
    }
    
    @Override
    public void run(String... args) throws Exception {
        // 只在指定参数时运行测试
        if (args.length > 0 && "stress-test".equals(args[0])) {
            runTest();
        }
    }
}
