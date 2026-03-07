package com.dianping.common.dto;

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
    private String refundReason;
    private LocalDateTime purchasedAt;

    public UserCouponView() {
    }

    public UserCouponView(Long purchaseId, Long couponId, Long shopId, String type, String title,
                          String description, BigDecimal discountAmount, BigDecimal price,
                          String status, String refundReason, LocalDateTime purchasedAt) {
        this.purchaseId = purchaseId;
        this.couponId = couponId;
        this.shopId = shopId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.discountAmount = discountAmount;
        this.price = price;
        this.status = status;
        this.refundReason = refundReason;
        this.purchasedAt = purchasedAt;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }
}
