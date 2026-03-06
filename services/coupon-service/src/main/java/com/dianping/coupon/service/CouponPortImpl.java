package com.dianping.coupon.service;

import com.dianping.common.dto.UserCouponView;
import com.dianping.common.port.CouponPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class CouponPortImpl implements CouponPort {
    private final UserCouponService userCouponService;

    public CouponPortImpl(UserCouponService userCouponService) {
        this.userCouponService = userCouponService;
    }

    @Override
    public List<UserCouponView> listByUser(Long userId) {
        return userCouponService.listByUser(userId);
    }
}
