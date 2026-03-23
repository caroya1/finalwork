package com.dianping.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 控制用户对数据的访问权限
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    
    /**
     * 权限类型
     */
    PermissionType type();
    
    /**
     * 实体类型（用于查询权限）
     * 例如：USER, MERCHANT, POST, SHOP
     */
    String entityType() default "";
    
    /**
     * 资源ID参数名（用于从方法参数中提取ID）
     * 默认为"id"
     */
    String resourceId() default "id";
}
