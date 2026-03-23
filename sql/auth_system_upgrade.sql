-- ============================================================
-- 鉴权系统升级 SQL 脚本
-- 包含：帖子可见性、用户隐私设置、审计日志表、角色权限表
-- ============================================================

-- -----------------------------------------------------------
-- 1. 帖子表添加可见性和状态字段
-- -----------------------------------------------------------
ALTER TABLE dp_post 
ADD COLUMN visibility VARCHAR(20) DEFAULT 'PUBLIC' COMMENT '可见性: PUBLIC-公开, PRIVATE-仅自己';

ALTER TABLE dp_post 
ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-正常, HIDDEN-用户隐藏, DELETED-已删除';

ALTER TABLE dp_post 
ADD COLUMN deleted_at DATETIME DEFAULT NULL COMMENT '删除时间，NULL表示未删除';

-- 更新现有数据
UPDATE dp_post SET visibility = 'PUBLIC', status = 'ACTIVE' WHERE visibility IS NULL;

-- 添加索引
CREATE INDEX idx_post_visibility ON dp_post(visibility);
CREATE INDEX idx_post_status ON dp_post(status);

-- -----------------------------------------------------------
-- 2. 用户隐私设置表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS dp_user_privacy_settings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    show_posts TINYINT DEFAULT 1 COMMENT '是否展示帖子列表: 0-否, 1-是',
    show_following TINYINT DEFAULT 1 COMMENT '是否展示关注列表: 0-否, 1-是',
    show_followers TINYINT DEFAULT 1 COMMENT '是否展示粉丝列表: 0-否, 1-是',
    show_phone TINYINT DEFAULT 0 COMMENT '是否展示手机号: 0-否, 1-是',
    show_email TINYINT DEFAULT 0 COMMENT '是否展示邮箱: 0-否, 1-是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户隐私设置表';

-- -----------------------------------------------------------
-- 3. 审计日志表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS dp_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_type VARCHAR(20) NOT NULL COMMENT '操作人类型: USER/ADMIN/SYSTEM',
    operator_name VARCHAR(64) COMMENT '操作人用户名',
    operation VARCHAR(100) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型',
    resource_id VARCHAR(100) COMMENT '资源ID',
    old_value JSON COMMENT '操作前的值',
    new_value JSON COMMENT '操作后的值',
    description VARCHAR(500) COMMENT '操作描述',
    status VARCHAR(20) NOT NULL COMMENT '状态: SUCCESS/FAILED',
    error_msg VARCHAR(500) COMMENT '错误信息',
    request_ip VARCHAR(50) COMMENT '请求IP',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_method VARCHAR(10) COMMENT '请求方法',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time_ms INT COMMENT '执行时间(毫秒)',
    PRIMARY KEY (id),
    KEY idx_operator (operator_id, operator_type),
    KEY idx_resource (resource_type, resource_id),
    KEY idx_operation (operation),
    KEY idx_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- -----------------------------------------------------------
-- 4. 角色表（支持动态权限）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS dp_roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL COMMENT '角色代码: USER, MERCHANT, ADMIN, SUPER_ADMIN',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统角色: 0-否, 1-是（系统角色不可删除）',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 插入初始角色数据
INSERT INTO dp_roles (role_code, role_name, description, is_system) VALUES
('USER', '普通用户', '平台普通用户', 1),
('MERCHANT', '商户', '入驻商户', 1),
('ADMIN', '管理员', '平台管理员', 1),
('SUPER_ADMIN', '超级管理员', '系统超级管理员', 1)
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- -----------------------------------------------------------
-- 5. 权限表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS dp_permissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    permission_code VARCHAR(100) NOT NULL COMMENT '权限代码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型: USER, MERCHANT, POST等',
    action VARCHAR(50) NOT NULL COMMENT '操作: READ, WRITE, DELETE, ADMIN等',
    description VARCHAR(255) COMMENT '权限描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    KEY idx_resource (resource_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 插入基础权限
INSERT INTO dp_permissions (permission_code, permission_name, resource_type, action) VALUES
('user:read', '查看用户信息', 'USER', 'READ'),
('user:write', '修改用户信息', 'USER', 'WRITE'),
('user:admin', '管理用户', 'USER', 'ADMIN'),
('merchant:read', '查看商户信息', 'MERCHANT', 'READ'),
('merchant:write', '修改商户信息', 'MERCHANT', 'WRITE'),
('merchant:admin', '管理商户', 'MERCHANT', 'ADMIN'),
('post:read', '查看帖子', 'POST', 'READ'),
('post:write', '发布帖子', 'POST', 'WRITE'),
('post:delete', '删除帖子', 'POST', 'DELETE'),
('post:admin', '管理帖子', 'POST', 'ADMIN')
ON DUPLICATE KEY UPDATE permission_name=VALUES(permission_name);

-- -----------------------------------------------------------
-- 6. 角色权限关联表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS dp_role_permissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================================
-- 完成
-- ============================================================
