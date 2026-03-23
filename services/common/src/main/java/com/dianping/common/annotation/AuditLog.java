package com.dianping.common.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 标记需要记录审计日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    
    /**
     * 操作类型
     */
    String operation();
    
    /**
     * 资源类型
     */
    String resourceType();
    
    /**
     * 资源ID表达式（支持SpEL）
     * 例如：#id, #request.userId
     */
    String resourceId() default "";
    
    /**
     * 操作描述
     */
    String description() default "";
}
