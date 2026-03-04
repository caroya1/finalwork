package com.dianping.coupon.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserCouponView {
    private Long purchaseId;
    private Long couponId;
    private Long shopId;
    private String type;
    private String title;
    private String description;
    private BigDecimal discountAmount;
    private BigDecimal price;
    private String status;
    private LocalDateTime purchasedAt;

    public UserCouponView(Long purchaseId, Long couponId, Long shopId, String type, String title,
                          String description, BigDecimal discountAmount, BigDecimal price,
                          String status, LocalDateTime purchasedAt) {
        this.purchaseId = purchaseId;
        this.couponId = couponId;
        this.shopId = shopId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.discountAmount = discountAmount;
        this.price = price;
        this.status = status;
        this.purchasedAt = purchasedAt;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }
}
