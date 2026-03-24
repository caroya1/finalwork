package com.dianping.merchant.enums;

public enum MerchantStatus {
    PENDING(0, "待审核"),
    NORMAL(1, "正常"),
    DISABLED(2, "禁用");

    private final int code;
    private final String desc;

    MerchantStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MerchantStatus fromCode(int code) {
        for (MerchantStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown merchant status code: " + code);
    }
}
