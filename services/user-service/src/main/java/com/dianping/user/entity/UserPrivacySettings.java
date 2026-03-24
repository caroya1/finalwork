package com.dianping.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户隐私设置
 */
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getShowPosts() {
        return showPosts;
    }

    public void setShowPosts(Boolean showPosts) {
        this.showPosts = showPosts;
    }

    public Boolean getShowFollowing() {
        return showFollowing;
    }

    public void setShowFollowing(Boolean showFollowing) {
        this.showFollowing = showFollowing;
    }

    public Boolean getShowFollowers() {
        return showFollowers;
    }

    public void setShowFollowers(Boolean showFollowers) {
        this.showFollowers = showFollowers;
    }

    public Boolean getShowPhone() {
        return showPhone;
    }

    public void setShowPhone(Boolean showPhone) {
        this.showPhone = showPhone;
    }

    public Boolean getShowEmail() {
        return showEmail;
    }

    public void setShowEmail(Boolean showEmail) {
        this.showEmail = showEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
