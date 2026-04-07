package com.dianping.coupon.service;

import com.dianping.common.dto.ConsumeCouponResult;
import com.dianping.common.dto.UserCouponView;
import com.dianping.common.port.CouponPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class CouponPortImpl implements CouponPort {
    private final UserCouponService userCouponService;
    private final CouponService couponService;

    public CouponPortImpl(UserCouponService userCouponService, CouponService couponService) {
        this.userCouponService = userCouponService;
        this.couponService = couponService;
    }

    @Override
    public List<UserCouponView> listByUser(Long userId) {
        return userCouponService.listByUser(userId);
    }

    @Override
    public ConsumeCouponResult consumeCoupon(Long userId, Long couponId, Long shopId, String orderNo) {
        return couponService.consumeCoupon(userId, couponId, shopId, orderNo);
    }

    @Override
    public void returnCoupon(Long purchaseId, String orderNo, String reason) {
        couponService.returnCoupon(purchaseId, orderNo, reason);
    }
}
