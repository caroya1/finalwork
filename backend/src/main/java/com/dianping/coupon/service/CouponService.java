package com.dianping.coupon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BusinessException;
import com.dianping.coupon.dto.CouponCreateRequest;
import com.dianping.coupon.dto.SeckillOrderMessage;
import com.dianping.coupon.entity.Coupon;
import com.dianping.coupon.entity.CouponPurchase;
import com.dianping.coupon.mapper.CouponMapper;
import com.dianping.coupon.mapper.CouponPurchaseMapper;
import com.dianping.common.service.UserFacade;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class CouponService {
    private static final String TYPE_NORMAL = "normal";
    private static final String TYPE_SECKILL = "seckill";
    private static final String SECKILL_STOCK_KEY_PREFIX = "dp:seckill:stock:";
    private static final String SECKILL_USER_KEY_PREFIX = "dp:seckill:user:";
    private static final String SECKILL_COUPON_KEY_PREFIX = "dp:seckill:coupon:";
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(
                "local stock = tonumber(redis.call('get', KEYS[1]));" +
                "if not stock then return -1 end;" +
                "if stock <= 0 then return 0 end;" +
                "if redis.call('exists', KEYS[2]) == 1 then return 2 end;" +
                "redis.call('decr', KEYS[1]);" +
                "redis.call('set', KEYS[2], '1', 'EX', ARGV[1]);" +
                "return 1;"
        );
        SECKILL_SCRIPT = script;
    }

    private final CouponMapper couponMapper;
    private final CouponPurchaseMapper couponPurchaseMapper;
    private final UserFacade userFacade;
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final RedissonClient redissonClient;
    private final String seckillQueueName;

    public CouponService(CouponMapper couponMapper,
                         CouponPurchaseMapper couponPurchaseMapper,
                         UserFacade userFacade,
                         RedisTemplate<String, Object> redisTemplate,
                         StringRedisTemplate stringRedisTemplate,
                         RabbitTemplate rabbitTemplate,
                         RedissonClient redissonClient,
                         @Value("${app.seckill.queue-name:dp.seckill.coupon.queue}") String seckillQueueName) {
        this.couponMapper = couponMapper;
        this.couponPurchaseMapper = couponPurchaseMapper;
        this.userFacade = userFacade;
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.redissonClient = redissonClient;
        this.seckillQueueName = seckillQueueName;
    }

    public Coupon create(CouponCreateRequest request) {
        Coupon coupon = new Coupon();
        coupon.setShopId(request.getShopId());
        coupon.setType(normalizeType(request.getType()));
        coupon.setTitle(request.getTitle());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountAmount(request.getDiscountAmount());
        coupon.setPrice(request.getPrice());
        if (TYPE_SECKILL.equals(coupon.getType())) {
            if (request.getTotalStock() == null || request.getTotalStock() <= 0) {
                throw new BusinessException("totalStock is required for seckill coupon");
            }
            coupon.setTotalStock(request.getTotalStock());
            Integer remaining = request.getRemainingStock() == null ? request.getTotalStock() : request.getRemainingStock();
            if (remaining <= 0) {
                throw new BusinessException("remainingStock must be positive");
            }
            coupon.setRemainingStock(Math.min(remaining, request.getTotalStock()));
            coupon.setStartTime(parseTime(request.getStartTime(), "startTime is required for seckill coupon"));
            coupon.setEndTime(parseTime(request.getEndTime(), "endTime is required for seckill coupon"));
        } else {
            if (request.getTotalStock() != null || request.getRemainingStock() != null) {
                throw new BusinessException("stock is not allowed for normal coupon");
            }
            if ((request.getStartTime() != null && !request.getStartTime().trim().isEmpty())
                    || (request.getEndTime() != null && !request.getEndTime().trim().isEmpty())) {
                throw new BusinessException("time window is not allowed for normal coupon");
            }
            coupon.setTotalStock(null);
            coupon.setRemainingStock(null);
            coupon.setStartTime(null);
            coupon.setEndTime(null);
        }
        if (coupon.getPrice() == null || coupon.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("price must be zero or positive");
        }
        if (coupon.getDiscountAmount() == null || coupon.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("discount amount must be positive");
        }
        if (coupon.getPrice().compareTo(coupon.getDiscountAmount()) >= 0) {
            throw new BusinessException("price must be less than discount amount");
        }
        coupon.touchForCreate();
        couponMapper.insert(coupon);
        cacheSeckillCoupon(coupon.getId());
        return coupon;
    }

    public List<Coupon> listByShop(Long shopId) {
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>().eq(Coupon::getShopId, shopId));
    }

    @Transactional(transactionManager = "couponTransactionManager")
    public CouponPurchase purchase(Long couponId, Long userId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException("coupon not found");
        }
        if (TYPE_SECKILL.equals(coupon.getType())) {
            return handleSeckillPurchase(coupon, userId);
        }

        BigDecimal price = coupon.getPrice() == null ? BigDecimal.ZERO : coupon.getPrice();
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            userFacade.deductBalance(userId, price);
        }

        CouponPurchase purchase = new CouponPurchase();
        purchase.setCouponId(coupon.getId());
        purchase.setUserId(userId);
        purchase.setAmount(price);
        purchase.setStatus("paid");
        purchase.touchForCreate();
        couponPurchaseMapper.insert(purchase);
        return purchase;
    }

    @Transactional(transactionManager = "couponTransactionManager")
    public CouponPurchase refund(Long purchaseId, Long userId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("refund reason is required");
        }
        CouponPurchase purchase = couponPurchaseMapper.selectById(purchaseId);
        if (purchase == null || !userId.equals(purchase.getUserId())) {
            throw new BusinessException("purchase not found");
        }
        if ("refunded".equals(purchase.getStatus())) {
            return purchase;
        }
        Coupon coupon = couponMapper.selectById(purchase.getCouponId());
        if (coupon == null) {
            throw new BusinessException("coupon not found");
        }
        BigDecimal amount = purchase.getAmount() == null ? BigDecimal.ZERO : purchase.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            userFacade.recharge(userId, amount);
        }
        purchase.setStatus("refunded");
        purchase.setRefundReason(reason.trim());
        purchase.setRefundedAt(LocalDateTime.now());
        couponPurchaseMapper.updateById(purchase);
        if (TYPE_SECKILL.equals(coupon.getType())) {
            coupon.setRemainingStock((coupon.getRemainingStock() == null ? 0 : coupon.getRemainingStock()) + 1);
            coupon.touchForUpdate();
            couponMapper.updateById(coupon);
        }
        return purchase;
    }

    private CouponPurchase handleSeckillPurchase(Coupon coupon, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
            throw new BusinessException("seckill not started");
        }
        if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
            throw new BusinessException("seckill ended");
        }
        String stockKey = SECKILL_STOCK_KEY_PREFIX + coupon.getId();
        String userKey = SECKILL_USER_KEY_PREFIX + coupon.getId() + ":" + userId;
        long ttlSeconds = resolveSeckillTtlSeconds(coupon, now);
        Long result = executeSeckillScript(stockKey, userKey, ttlSeconds);
        if (result == null || result == -1L) {
            cacheSeckillCoupon(coupon);
            result = executeSeckillScript(stockKey, userKey, ttlSeconds);
        }
        if (result == null || result == -1L) {
            throw new BusinessException("seckill not ready");
        }
        if (result == 0L) {
            throw new BusinessException("coupon sold out");
        }
        if (result == 2L) {
            throw new BusinessException("already purchased");
        }
        CouponPurchase purchase = new CouponPurchase();
        purchase.setCouponId(coupon.getId());
        purchase.setUserId(userId);
        purchase.setAmount(coupon.getPrice() == null ? BigDecimal.ZERO : coupon.getPrice());
        purchase.setStatus("processing");
        purchase.touchForCreate();
        couponPurchaseMapper.insert(purchase);
        rabbitTemplate.convertAndSend(seckillQueueName, new SeckillOrderMessage(coupon.getId(), userId));
        return purchase;
    }

    public void handleSeckillOrder(Long couponId, Long userId) {
        String lockKey = "dp:seckill:lock:" + couponId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException("seckill busy");
            }
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null) {
                throw new BusinessException("coupon not found");
            }
            if (coupon.getRemainingStock() == null || coupon.getRemainingStock() <= 0) {
                throw new BusinessException("coupon sold out");
            }
            CouponPurchase existing = couponPurchaseMapper.selectOne(new LambdaQueryWrapper<CouponPurchase>()
                    .eq(CouponPurchase::getCouponId, couponId)
                    .eq(CouponPurchase::getUserId, userId)
                    .orderByDesc(CouponPurchase::getCreatedAt)
                    .last("LIMIT 1"));
            if (existing != null && "paid".equals(existing.getStatus())) {
                return;
            }
            BigDecimal price = coupon.getPrice() == null ? BigDecimal.ZERO : coupon.getPrice();
            if (price.compareTo(BigDecimal.ZERO) > 0) {
                userFacade.deductBalance(userId, price);
            }
            coupon.setRemainingStock(coupon.getRemainingStock() - 1);
            coupon.touchForUpdate();
            couponMapper.updateById(coupon);
            if (existing != null) {
                existing.setStatus("paid");
                couponPurchaseMapper.updateById(existing);
            } else {
                CouponPurchase purchase = new CouponPurchase();
                purchase.setCouponId(couponId);
                purchase.setUserId(userId);
                purchase.setAmount(price);
                purchase.setStatus("paid");
                purchase.touchForCreate();
                couponPurchaseMapper.insert(purchase);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException("seckill interrupted");
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void cacheSeckillCoupon(Coupon coupon) {
        long ttlSeconds = resolveSeckillTtlSeconds(coupon, LocalDateTime.now());
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY_PREFIX + coupon.getId(),
                String.valueOf(coupon.getRemainingStock()), ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(SECKILL_COUPON_KEY_PREFIX + coupon.getId(), coupon, ttlSeconds, TimeUnit.SECONDS);
    }

    public void cacheSeckillCoupon(Long couponId) {
        if (couponId == null) {
            return;
        }
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || !TYPE_SECKILL.equals(coupon.getType())) {
            return;
        }
        cacheSeckillCoupon(coupon);
    }

    private Long executeSeckillScript(String stockKey, String userKey, long ttlSeconds) {
        return stringRedisTemplate.execute(SECKILL_SCRIPT, Arrays.asList(stockKey, userKey), String.valueOf(ttlSeconds));
    }

    private long resolveSeckillTtlSeconds(Coupon coupon, LocalDateTime now) {
        if (coupon.getEndTime() != null) {
            long seconds = Duration.between(now, coupon.getEndTime()).getSeconds();
            if (seconds > 0) {
                return Math.max(seconds, 60);
            }
        }
        return TimeUnit.DAYS.toSeconds(1);
    }

    private String normalizeType(String type) {
        if (type == null) {
            throw new BusinessException("type is required");
        }
        String value = type.trim().toLowerCase();
        if (TYPE_NORMAL.equals(value) || "normal".equals(value)) {
            return TYPE_NORMAL;
        }
        if (TYPE_SECKILL.equals(value) || "seckill".equals(value)) {
            return TYPE_SECKILL;
        }
        throw new BusinessException("invalid coupon type");
    }

    private LocalDateTime parseTime(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(message);
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("time format must be ISO_LOCAL_DATE_TIME");
        }
    }
}
