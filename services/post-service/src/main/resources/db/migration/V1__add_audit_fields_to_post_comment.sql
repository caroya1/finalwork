-- Add audit fields to post_comment table
ALTER TABLE post_comment 
ADD COLUMN audit_status INT DEFAULT 0 COMMENT '审核状态: 0-待审核 1-通过 2-拒绝';

ALTER TABLE post_comment 
ADD COLUMN audit_remark VARCHAR(255) DEFAULT NULL COMMENT '审核备注';