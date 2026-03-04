package com.dianping.coupon.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.coupon.dto.CouponCreateRequest;
import com.dianping.coupon.dto.CouponPurchaseRequest;
import com.dianping.coupon.entity.Coupon;
import com.dianping.coupon.entity.CouponPurchase;
import com.dianping.coupon.service.CouponService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@Validated
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ApiResponse<Coupon> create(@Valid @RequestBody CouponCreateRequest request) {
        return ApiResponse.ok(couponService.create(request));
    }

    @GetMapping
    public ApiResponse<List<Coupon>> listByShop(@RequestParam("shopId") Long shopId) {
        return ApiResponse.ok(couponService.listByShop(shopId));
    }

    @PostMapping("/{id}/purchase")
    public ApiResponse<CouponPurchase> purchase(@PathVariable("id") Long id,
                                                @Valid @RequestBody CouponPurchaseRequest request) {
        return ApiResponse.ok(couponService.purchase(id, request.getUserId()));
    }
}
