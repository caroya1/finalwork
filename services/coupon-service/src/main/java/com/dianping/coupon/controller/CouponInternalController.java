package com.dianping.coupon.controller;

import com.dianping.common.dto.UserCouponView;
import com.dianping.common.port.CouponPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/coupons")
public class CouponInternalController {
    private final CouponPort couponPort;

    public CouponInternalController(CouponPort couponPort) {
        this.couponPort = couponPort;
    }

    @GetMapping("/user")
    public List<UserCouponView> listByUser(@RequestParam("userId") Long userId) {
        return couponPort.listByUser(userId);
    }
}
