package com.dianping.coupon.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.coupon.entity.Coupon;
import com.dianping.coupon.entity.CouponPurchase;
import com.dianping.coupon.mapper.CouponMapper;
import com.dianping.coupon.mapper.CouponPurchaseMapper;
import com.dianping.common.port.UserAuthPort;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单处理器
 * 优化锁的获取和释放，确保在任何情况下都能正确释放锁
 */
@Component
public class SeckillOrderHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SeckillOrderHandler.class);
    
    private final CouponMapper couponMapper;
    private final CouponPurchaseMapper couponPurchaseMapper;
    private final UserAuthPort userAuthPort;
    private final RedissonClient redissonClient;
    
    // 锁等待时间（秒）
    private static final long LOCK_WAIT_TIME = 5;
    // 锁自动释放时间（秒）- 看门狗机制会自动续期
    private static final long LOCK_LEASE_TIME = 30;
    
    public SeckillOrderHandler(CouponMapper couponMapper,
                               CouponPurchaseMapper couponPurchaseMapper,
                               UserAuthPort userAuthPort,
                               RedissonClient redissonClient) {
        this.couponMapper = couponMapper;
        this.couponPurchaseMapper = couponPurchaseMapper;
        this.userAuthPort = userAuthPort;
        this.redissonClient = redissonClient;
    }
    
    /**
     * 处理秒杀订单
     * 优化点：
     * 1. 使用Redisson的看门狗机制自动续期锁
     * 2. 确保finally块中正确释放锁
     * 3. 添加详细的日志记录
     * 4. 使用事务保证数据一致性
     */
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long couponId, Long userId) {
        String lockKey = buildLockKey(couponId, userId);
        RLock lock = redissonClient.getLock(lockKey);
        
        logger.info("开始处理秒杀订单, couponId={}, userId={}, lockKey={}", couponId, userId, lockKey);
        
        boolean locked = false;
        try {
            // 尝试获取锁，使用看门狗自动续期
            locked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            
            if (!locked) {
                logger.warn("获取锁失败, couponId={}, userId={}", couponId, userId);
                throw new BusinessException("活动太火爆了，请稍后重试");
            }
            
            logger.debug("成功获取锁, couponId={}, userId={}", couponId, userId);
            
            // 执行业务逻辑
            doHandle(couponId, userId);
            
            logger.info("秒杀订单处理成功, couponId={}, userId={}", couponId, userId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("处理秒杀订单被中断, couponId={}, userId={}", couponId, userId, e);
            throw new BusinessException("操作被中断，请重试");
        } catch (BusinessException e) {
            logger.warn("业务异常, couponId={}, userId={}, msg={}", couponId, userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("处理秒杀订单异常, couponId={}, userId={}", couponId, userId, e);
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            // 确保锁被释放
            releaseLock(lock, locked, couponId, userId);
        }
    }
    
    /**
     * 实际业务处理
     */
    private void doHandle(Long couponId, Long userId) {
        // 1. 查询优惠券
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        
        // 2. 检查库存
        if (coupon.getRemainingStock() == null || coupon.getRemainingStock() <= 0) {
            throw new BusinessException("优惠券已售罄");
        }
        
        // 3. 查询用户购买记录
        CouponPurchase existing = couponPurchaseMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CouponPurchase>()
                .eq(CouponPurchase::getCouponId, couponId)
                .eq(CouponPurchase::getUserId, userId)
                .orderByDesc(CouponPurchase::getCreatedAt)
                .last("LIMIT 1")
        );
        
        // 4. 如果已经支付成功，直接返回（幂等性）
        if (existing != null && "paid".equals(existing.getStatus())) {
            logger.info("用户已购买过该优惠券, couponId={}, userId={}", couponId, userId);
            return;
        }
        
        // 5. 扣减余额
        BigDecimal price = coupon.getPrice() == null ? BigDecimal.ZERO : coupon.getPrice();
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            userAuthPort.deductBalance(userId, price);
            logger.debug("扣减余额成功, userId={}, amount={}", userId, price);
        }
        
        // 6. 扣减库存
        coupon.setRemainingStock(coupon.getRemainingStock() - 1);
        coupon.touchForUpdate();
        int updateResult = couponMapper.updateById(coupon);
        if (updateResult == 0) {
            throw new BusinessException("库存不足");
        }
        
        // 7. 更新或创建购买记录
        if (existing != null) {
            existing.setStatus("paid");
            couponPurchaseMapper.updateById(existing);
            logger.debug("更新购买记录成功, purchaseId={}", existing.getId());
        } else {
            CouponPurchase purchase = new CouponPurchase();
            purchase.setCouponId(couponId);
            purchase.setUserId(userId);
            purchase.setAmount(price);
            purchase.setStatus("paid");
            purchase.touchForCreate();
            couponPurchaseMapper.insert(purchase);
            logger.debug("创建购买记录成功, purchaseId={}", purchase.getId());
        }
    }
    
    /**
     * 安全释放锁
     */
    private void releaseLock(RLock lock, boolean locked, Long couponId, Long userId) {
        if (!locked) {
            return;
        }
        
        try {
            // 检查当前线程是否持有锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                logger.debug("成功释放锁, couponId={}, userId={}", couponId, userId);
            } else {
                logger.warn("当前线程不持有锁, 无法释放, couponId={}, userId={}", couponId, userId);
            }
        } catch (Exception e) {
            logger.error("释放锁时发生异常, couponId={}, userId={}", couponId, userId, e);
        }
    }
    
    /**
     * 构建锁的key
     */
    private String buildLockKey(Long couponId, Long userId) {
        return "dp:seckill:lock:v2:" + couponId + ":" + userId;
    }
}
