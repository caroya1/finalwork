package com.dianping.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志实体
 */
@Data
@TableName("dp_audit_log")
public class AuditLogEntry {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long operatorId;
    private String operatorType;
    private String operatorName;
    
    private String operation;
    private String resourceType;
    private String resourceId;
    
    private String oldValue;
    private String newValue;
    private String description;
    
    private String status;
    private String errorMsg;
    
    private String requestIp;
    private String requestUrl;
    private String requestMethod;
    private String userAgent;
    
    private LocalDateTime operationTime;
    private Integer executionTimeMs;
}
