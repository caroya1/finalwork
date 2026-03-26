package com.dianping.common.dto;

import com.dianping.common.enums.AuditStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditResultTest {

    @Test
    void testDefaultConstructor() {
        AuditResult result = new AuditResult();
        assertNull(result.getApproved());
        assertNull(result.getReason());
        assertNull(result.getConfidence());
        assertNull(result.getAuditType());
    }

    @Test
    void testAllArgsConstructor() {
        AuditResult result = new AuditResult(true, "Approved", 0.95, "content");
        
        assertTrue(result.getApproved());
        assertEquals("Approved", result.getReason());
        assertEquals(0.95, result.getConfidence());
        assertEquals("content", result.getAuditType());
    }

    @Test
    void testSetters() {
        AuditResult result = new AuditResult();
        result.setApproved(false);
        result.setReason("Rejected for spam");
        result.setConfidence(0.3);
        result.setAuditType("image");
        
        assertFalse(result.getApproved());
        assertEquals("Rejected for spam", result.getReason());
        assertEquals(0.3, result.getConfidence());
        assertEquals("image", result.getAuditType());
    }
}
