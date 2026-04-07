package com.dianping.order.client;

import com.dianping.common.dto.ConsumeCouponResult;
import com.dianping.common.dto.UserCouponView;
import com.dianping.common.port.CouponPort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "coupon-service")
public interface CouponClient extends CouponPort {

    @Override
    @GetMapping("/internal/coupons/user")
    List<UserCouponView> listByUser(@RequestParam("userId") Long userId);

    @Override
    @PostMapping("/internal/coupons/consume")
    ConsumeCouponResult consumeCoupon(
            @RequestParam("userId") Long userId,
            @RequestParam("couponId") Long couponId,
            @RequestParam("shopId") Long shopId,
            @RequestParam("orderNo") String orderNo);

    @Override
    @PostMapping("/internal/coupons/return")
    void returnCoupon(
            @RequestParam("purchaseId") Long purchaseId,
            @RequestParam("orderNo") String orderNo,
            @RequestParam("reason") String reason);
}