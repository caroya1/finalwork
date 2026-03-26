-- 更新所有现有帖子的审核状态为已通过
UPDATE dp_post SET audit_status = 1 WHERE audit_status IS NULL OR audit_status = 0;

-- 设置默认值为1
ALTER TABLE dp_post MODIFY COLUMN audit_status INT DEFAULT 1 COMMENT '审核状态: 0-待审核 1-通过 2-拒绝';