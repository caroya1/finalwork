package com.dianping.common.aspect;

import com.alibaba.fastjson2.JSON;
import com.dianping.common.annotation.AuditLog;
import com.dianping.common.context.UserContext;
import com.dianping.common.context.UserSession;
import com.dianping.common.entity.AuditLogEntry;
import com.dianping.common.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 审计日志切面
 * 双写：MySQL + ELK
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "audit.log", name = "enabled", havingValue = "true", matchIfMissing = false)
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    @Autowired
    private AuditLogService auditLogService;
    
    // 审计日志专用logger（发送到ELK）
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT_LOGGER");

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        
        UserSession session = UserContext.get();
        if (session == null) {
            // 未登录的操作不记录（或者记录为匿名）
            return point.proceed();
        }

        // 构建审计日志
        AuditLogEntry entry = buildAuditLogEntry(point, auditLog, session);
        long startTime = System.currentTimeMillis();

        try {
            Object result = point.proceed();
            
            // 记录成功
            entry.setStatus("SUCCESS");
            entry.setExecutionTimeMs((int) (System.currentTimeMillis() - startTime));
            
            // 双写
            saveAuditLog(entry);
            
            return result;
            
        } catch (Exception e) {
            // 记录失败
            entry.setStatus("FAILED");
            entry.setErrorMsg(e.getMessage());
            entry.setExecutionTimeMs((int) (System.currentTimeMillis() - startTime));
            
            saveAuditLog(entry);
            
            throw e;
        }
    }

    private AuditLogEntry buildAuditLogEntry(ProceedingJoinPoint point, 
                                             AuditLog auditLog, 
                                             UserSession session) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setOperatorId(session.getId());
        entry.setOperatorType(session.isAdmin() ? "ADMIN" : "USER");
        entry.setOperatorName(session.getUsername());
        entry.setOperation(auditLog.operation());
        entry.setResourceType(auditLog.resourceType());
        entry.setDescription(auditLog.description());
        entry.setOperationTime(LocalDateTime.now());
        
        // 提取资源ID
        Object[] args = point.getArgs();
        if (args.length > 0 && args[0] != null) {
            entry.setResourceId(args[0].toString());
        }
        
        // 请求信息
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            entry.setRequestIp(getClientIp(request));
            entry.setRequestUrl(request.getRequestURI());
            entry.setRequestMethod(request.getMethod());
            entry.setUserAgent(request.getHeader("User-Agent"));
        }
        
        return entry;
    }

    private void saveAuditLog(AuditLogEntry entry) {
        try {
            // 1. 写入MySQL
            auditLogService.save(entry);
        } catch (Exception e) {
            log.error("保存审计日志到MySQL失败", e);
        }
        
        try {
            // 2. 发送到ELK（通过Logback）
            AUDIT_LOGGER.info(JSON.toJSONString(entry));
        } catch (Exception e) {
            log.error("发送审计日志到ELK失败", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
