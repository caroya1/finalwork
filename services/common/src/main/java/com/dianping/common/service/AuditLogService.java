package com.dianping.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dianping.common.entity.AuditLogEntry;
import com.dianping.common.mapper.AuditLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务
 */
@Service
@Slf4j
public class AuditLogService extends ServiceImpl<AuditLogMapper, AuditLogEntry> {
    
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
