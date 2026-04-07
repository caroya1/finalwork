package com.dianping.coupon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券购买记录实体
 * 
 * Status lifecycle:
 * - paid: 已支付，可使用
 * - processing: 处理中（如秒杀抢购中）
 * - used: 已使用（创建订单后）
 * - refunded: 已退款
 * 
 * State transitions:
 * - paid → used (on order create)
 * - used → paid (on cancel/timeout return)
 * - paid → refunded (explicit refund)
 */
@TableName("dp_coupon_purchase")
public class CouponPurchase {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long couponId;

    private Long userId;

    private BigDecimal amount;

    private String status;

    private String refundReason;

    private LocalDateTime refundedAt;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;

    public void touchForCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }
}
