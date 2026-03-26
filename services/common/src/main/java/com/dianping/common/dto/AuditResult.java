package com.dianping.common.dto;

import java.io.Serializable;

public class AuditResult implements Serializable {
    private Boolean approved;
    private String reason;
    private Double confidence;
    private String auditType;

    public AuditResult() {
    }

    public AuditResult(Boolean approved, String reason, Double confidence, String auditType) {
        this.approved = approved;
        this.reason = reason;
        this.confidence = confidence;
        this.auditType = auditType;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getAuditType() {
        return auditType;
    }

    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }
}
