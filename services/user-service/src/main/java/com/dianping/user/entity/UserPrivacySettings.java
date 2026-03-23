package com.dianping.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户隐私设置
 */
@Data
@TableName("dp_user_privacy_settings")
public class UserPrivacySettings {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    // 帖子相关
    private Boolean showPosts;
    
    // 关注相关
    private Boolean showFollowing;
    private Boolean showFollowers;
    
    // 个人信息
    private Boolean showPhone;
    private Boolean showEmail;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
