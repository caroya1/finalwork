package com.dianping.common.dto;

/**
 * 优惠券核销请求
 */
public class ConsumeCouponRequest {
    private Long userId;
    private Long couponId;
    private Long shopId;
    private String orderNo;

    public ConsumeCouponRequest() {
    }

    public ConsumeCouponRequest(Long userId, Long couponId, Long shopId, String orderNo) {
        this.userId = userId;
        this.couponId = couponId;
        this.shopId = shopId;
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
