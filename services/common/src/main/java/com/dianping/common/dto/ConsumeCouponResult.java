package com.dianping.common.dto;

import java.math.BigDecimal;

/**
 * 优惠券核销结果
 */
public class ConsumeCouponResult {
    private Long purchaseId;
    private BigDecimal discountAmount;

    public ConsumeCouponResult() {
    }

    public ConsumeCouponResult(Long purchaseId, BigDecimal discountAmount) {
        this.purchaseId = purchaseId;
        this.discountAmount = discountAmount;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}
