package com.dianping.common.enums;

public enum AuditStatus {
    PENDING(0),
    APPROVED(1),
    REJECTED(2);

    private final int code;

    AuditStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static AuditStatus fromCode(int code) {
        for (AuditStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown audit status code: " + code);
    }
}
