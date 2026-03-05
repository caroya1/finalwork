package com.dianping.coupon.dto;

import java.io.Serializable;

public class SeckillOrderMessage implements Serializable {
    private Long couponId;
    private Long userId;

    public SeckillOrderMessage() {
    }

    public SeckillOrderMessage(Long couponId, Long userId) {
        this.couponId = couponId;
        this.userId = userId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
