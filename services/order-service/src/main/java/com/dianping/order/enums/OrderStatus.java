package com.dianping.order.enums;

public enum OrderStatus {
    PENDING_PAYMENT(0, "待支付"),
    PAID(1, "已支付"),
    VERIFIED(2, "已核销"),
    REFUNDED(3, "已退款"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static OrderStatus fromCode(int code) {
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status code: " + code);
    }

    public boolean canTransitionTo(OrderStatus target) {
        switch (this) {
            case PENDING_PAYMENT:
                return target == PAID || target == CANCELLED;
            case PAID:
                return target == VERIFIED || target == REFUNDED;
            case VERIFIED:
            case REFUNDED:
            case CANCELLED:
                return false;
            default:
                return false;
        }
    }
}
