package com.dianping.coupon.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.dto.ConsumeCouponRequest;
import com.dianping.common.dto.ConsumeCouponResult;
import com.dianping.common.dto.ReturnCouponRequest;
import com.dianping.common.dto.UserCouponView;
import com.dianping.common.port.CouponPort;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/consume")
    public ConsumeCouponResult consume(@RequestBody ConsumeCouponRequest request) {
        return couponPort.consumeCoupon(
                request.getUserId(),
                request.getCouponId(),
                request.getShopId(),
                request.getOrderNo()
        );
    }

    @PostMapping("/return")
    public ApiResponse<Void> returnCoupon(@RequestBody ReturnCouponRequest request) {
        couponPort.returnCoupon(
                request.getPurchaseId(),
                request.getOrderNo(),
                request.getReason()
        );
        return ApiResponse.ok(null);
    }
}
