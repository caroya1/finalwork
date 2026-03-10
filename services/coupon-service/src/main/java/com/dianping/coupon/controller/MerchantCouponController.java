package com.dianping.coupon.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.exception.BusinessException;
import com.dianping.coupon.dto.CouponCreateRequest;
import com.dianping.coupon.entity.Coupon;
import com.dianping.coupon.service.CouponService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/coupons")
@Validated
public class MerchantCouponController {
    private final CouponService couponService;

    public MerchantCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ApiResponse<Coupon> create(@Valid @RequestBody CouponCreateRequest request) {
        Coupon coupon = couponService.create(request);
        couponService.cacheSeckillCoupon(coupon.getId());
        return ApiResponse.ok(coupon);
    }

    @GetMapping("/shop/{shopId}")
    public ApiResponse<List<Coupon>> listByShop(@PathVariable("shopId") Long shopId) {
        return ApiResponse.ok(couponService.listByShop(shopId));
    }
}
