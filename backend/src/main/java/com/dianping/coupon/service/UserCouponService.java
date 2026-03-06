package com.dianping.coupon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.dto.UserCouponView;
import com.dianping.coupon.entity.Coupon;
import com.dianping.coupon.entity.CouponPurchase;
import com.dianping.coupon.mapper.CouponMapper;
import com.dianping.coupon.mapper.CouponPurchaseMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class UserCouponService {
    private final CouponPurchaseMapper couponPurchaseMapper;
    private final CouponMapper couponMapper;
    private final Executor appTaskExecutor;

    public UserCouponService(CouponPurchaseMapper couponPurchaseMapper, CouponMapper couponMapper,
                             @Qualifier("appTaskExecutor") Executor appTaskExecutor) {
        this.couponPurchaseMapper = couponPurchaseMapper;
        this.couponMapper = couponMapper;
        this.appTaskExecutor = appTaskExecutor;
    }

    public List<UserCouponView> listByUser(Long userId) {
        List<CouponPurchase> purchases = couponPurchaseMapper.selectList(
                new LambdaQueryWrapper<CouponPurchase>().eq(CouponPurchase::getUserId, userId)
                        .orderByDesc(CouponPurchase::getCreatedAt)
        );
        List<UserCouponView> result = new ArrayList<>();
        List<CompletableFuture<UserCouponView>> futures = new ArrayList<>();
        for (CouponPurchase purchase : purchases) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                Coupon coupon = couponMapper.selectById(purchase.getCouponId());
                if (coupon == null) {
                    return null;
                }
                return new UserCouponView(
                        purchase.getId(),
                        coupon.getId(),
                        coupon.getShopId(),
                        coupon.getType(),
                        coupon.getTitle(),
                        coupon.getDescription(),
                        coupon.getDiscountAmount(),
                        coupon.getPrice(),
                        purchase.getStatus(),
                        purchase.getRefundReason(),
                        purchase.getCreatedAt()
                );
            }, appTaskExecutor));
        }
        for (CompletableFuture<UserCouponView> future : futures) {
            UserCouponView view = future.join();
            if (view != null) {
                result.add(view);
            }
        }
        return result;
    }
}
