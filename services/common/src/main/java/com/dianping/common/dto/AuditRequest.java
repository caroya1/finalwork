package com.dianping.common.dto;

import java.io.Serializable;

public class AuditRequest implements Serializable {
    private String content;
    private String type;
    private Long targetId;

    public AuditRequest() {
    }

    public AuditRequest(String content, String type, Long targetId) {
        this.content = content;
        this.type = type;
        this.targetId = targetId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
}
