package com.dianping.user.client;

import com.dianping.common.dto.UserCouponView;
import com.dianping.common.port.CouponPort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "coupon-service")
public interface CouponClient extends CouponPort {
    @Override
    @GetMapping("/internal/coupons/user")
    List<UserCouponView> listByUser(@RequestParam("userId") Long userId);
}
