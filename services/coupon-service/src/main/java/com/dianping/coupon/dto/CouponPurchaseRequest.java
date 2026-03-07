package com.dianping.coupon.dto;

import jakarta.validation.constraints.NotNull;

public class CouponPurchaseRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    private String action;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
