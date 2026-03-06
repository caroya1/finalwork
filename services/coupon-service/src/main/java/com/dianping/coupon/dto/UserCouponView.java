package com.dianping.coupon.dto;

@Deprecated
public class UserCouponView extends com.dianping.common.dto.UserCouponView {
    public UserCouponView(Long purchaseId, Long couponId, Long shopId, String type, String title,
                          String description, java.math.BigDecimal discountAmount, java.math.BigDecimal price,
                          String status, String refundReason, java.time.LocalDateTime purchasedAt) {
        super(purchaseId, couponId, shopId, type, title, description, discountAmount, price, status, refundReason, purchasedAt);
    }
}
