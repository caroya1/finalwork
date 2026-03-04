package com.dianping.coupon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BusinessException;
import com.dianping.coupon.dto.CouponCreateRequest;
import com.dianping.coupon.entity.Coupon;
import com.dianping.coupon.entity.CouponPurchase;
import com.dianping.coupon.mapper.CouponMapper;
import com.dianping.coupon.mapper.CouponPurchaseMapper;
import com.dianping.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class CouponService {
    private static final String TYPE_NORMAL = "normal";
    private static final String TYPE_SECKILL = "seckill";

    private final CouponMapper couponMapper;
    private final CouponPurchaseMapper couponPurchaseMapper;
    private final UserService userService;

    public CouponService(CouponMapper couponMapper,
                         CouponPurchaseMapper couponPurchaseMapper,
                         UserService userService) {
        this.couponMapper = couponMapper;
        this.couponPurchaseMapper = couponPurchaseMapper;
        this.userService = userService;
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
        return coupon;
    }

    public List<Coupon> listByShop(Long shopId) {
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>().eq(Coupon::getShopId, shopId));
    }

    @Transactional
    public CouponPurchase purchase(Long couponId, Long userId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException("coupon not found");
        }
        if (TYPE_SECKILL.equals(coupon.getType())) {
            LocalDateTime now = LocalDateTime.now();
            if (coupon.getStartTime() != null && now.isBefore(coupon.getStartTime())) {
                throw new BusinessException("seckill not started");
            }
            if (coupon.getEndTime() != null && now.isAfter(coupon.getEndTime())) {
                throw new BusinessException("seckill ended");
            }
            if (coupon.getRemainingStock() == null || coupon.getRemainingStock() <= 0) {
                throw new BusinessException("coupon sold out");
            }
            coupon.setRemainingStock(coupon.getRemainingStock() - 1);
            coupon.touchForUpdate();
            couponMapper.updateById(coupon);
        }

        BigDecimal price = coupon.getPrice() == null ? BigDecimal.ZERO : coupon.getPrice();
        if (price.compareTo(BigDecimal.ZERO) > 0) {
            userService.deductBalance(userId, price);
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
