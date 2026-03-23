package com.dianping.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dianping.common.entity.AuditLogEntry;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志Mapper
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntry> {
}
