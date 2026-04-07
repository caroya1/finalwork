package com.dianping.common.dto;

/**
 * 优惠券退回请求
 */
public class ReturnCouponRequest {
    private Long purchaseId;
    private String orderNo;
    private String reason;

    public ReturnCouponRequest() {
    }

    public ReturnCouponRequest(Long purchaseId, String orderNo, String reason) {
        this.purchaseId = purchaseId;
        this.orderNo = orderNo;
        this.reason = reason;
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
