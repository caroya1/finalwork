package com.dianping.common.annotation;

/**
 * 数据权限类型
 */
public enum PermissionType {
    /**
     * 公开数据，任何人可访问
     */
    PUBLIC,
    
    /**
     * 仅所有者（用户ID匹配）
     * 例如：用户只能查看自己的订单
     */
    OWNER_ONLY,
    
    /**
     * 商户自己的数据（商户ID匹配）
     * 例如：商户只能管理自己的店铺
     */
    MERCHANT_OWNED,
    
    /**
     * 仅管理员可访问
     */
    ADMIN_ONLY
}
