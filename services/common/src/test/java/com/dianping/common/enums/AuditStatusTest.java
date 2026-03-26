package com.dianping.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditStatusTest {

    @Test
    void testEnumValues() {
        assertEquals(3, AuditStatus.values().length);
        assertEquals(AuditStatus.PENDING, AuditStatus.values()[0]);
        assertEquals(AuditStatus.APPROVED, AuditStatus.values()[1]);
        assertEquals(AuditStatus.REJECTED, AuditStatus.values()[2]);
    }

    @Test
    void testPendingCode() {
        assertEquals(0, AuditStatus.PENDING.getCode());
    }

    @Test
    void testApprovedCode() {
        assertEquals(1, AuditStatus.APPROVED.getCode());
    }

    @Test
    void testRejectedCode() {
        assertEquals(2, AuditStatus.REJECTED.getCode());
    }

    @Test
    void testFromCodePending() {
        assertEquals(AuditStatus.PENDING, AuditStatus.fromCode(0));
    }

    @Test
    void testFromCodeApproved() {
        assertEquals(AuditStatus.APPROVED, AuditStatus.fromCode(1));
    }

    @Test
    void testFromCodeRejected() {
        assertEquals(AuditStatus.REJECTED, AuditStatus.fromCode(2));
    }

    @Test
    void testFromCodeInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            AuditStatus.fromCode(99);
        });
    }
}