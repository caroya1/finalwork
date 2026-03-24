package com.dianping.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dianping.common.entity.AuditLogEntry;
import com.dianping.common.mapper.AuditLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务
 */
@Service
@ConditionalOnBean(AuditLogMapper.class)
public class AuditLogService extends ServiceImpl<AuditLogMapper, AuditLogEntry> {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    
    @Override
    public boolean save(AuditLogEntry entity) {
        try {
            return super.save(entity);
        } catch (Exception e) {
            log.error("保存审计日志失败: {}", entity, e);
            return false;
        }
    }
}
