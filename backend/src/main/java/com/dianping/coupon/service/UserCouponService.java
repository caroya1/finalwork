package com.dianping.coupon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.coupon.dto.UserCouponView;
import com.dianping.coupon.entity.Coupon;
import com.dianping.coupon.entity.CouponPurchase;
import com.dianping.coupon.mapper.CouponMapper;
import com.dianping.coupon.mapper.CouponPurchaseMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserCouponService {
    private final CouponPurchaseMapper couponPurchaseMapper;
    private final CouponMapper couponMapper;

    public UserCouponService(CouponPurchaseMapper couponPurchaseMapper, CouponMapper couponMapper) {
        this.couponPurchaseMapper = couponPurchaseMapper;
        this.couponMapper = couponMapper;
    }

    public List<UserCouponView> listByUser(Long userId) {
        List<CouponPurchase> purchases = couponPurchaseMapper.selectList(
                new LambdaQueryWrapper<CouponPurchase>().eq(CouponPurchase::getUserId, userId)
                        .orderByDesc(CouponPurchase::getCreatedAt)
        );
        List<UserCouponView> result = new ArrayList<>();
        for (CouponPurchase purchase : purchases) {
            Coupon coupon = couponMapper.selectById(purchase.getCouponId());
            if (coupon == null) {
                continue;
            }
            result.add(new UserCouponView(
                    purchase.getId(),
                    coupon.getId(),
                    coupon.getShopId(),
                    coupon.getType(),
                    coupon.getTitle(),
                    coupon.getDescription(),
                    coupon.getDiscountAmount(),
                    coupon.getPrice(),
                    purchase.getStatus(),
                    purchase.getCreatedAt()
            ));
        }
        return result;
    }
}
