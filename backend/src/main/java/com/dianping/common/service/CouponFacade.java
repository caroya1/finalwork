package com.dianping.common.service;

import com.dianping.common.dto.UserCouponView;
import com.dianping.coupon.service.UserCouponService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponFacade {
    private final UserCouponService userCouponService;

    public CouponFacade(UserCouponService userCouponService) {
        this.userCouponService = userCouponService;
    }

    public List<UserCouponView> listByUser(Long userId) {
        return userCouponService.listByUser(userId);
    }
}
