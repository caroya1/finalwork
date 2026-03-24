package com.dianping.user.controller;

import com.dianping.common.annotation.AuditLog;
import com.dianping.common.api.ApiResponse;
import com.dianping.common.context.UserContext;
import com.dianping.common.context.UserSession;
import com.dianping.common.exception.BusinessException;
import com.dianping.common.service.PermissionCacheService;
import com.dianping.user.entity.User;
import com.dianping.user.entity.UserPrivacySettings;
import com.dianping.user.service.UserPrivacyService;
import com.dianping.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员权限管理Controller
 * 用于动态调整用户权限和隐私设置
 */
@RestController
@RequestMapping("/api/admin/permissions")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminPermissionController {

    private static final Logger log = LoggerFactory.getLogger(AdminPermissionController.class);

    @Autowired
    private UserService userService;
    
    @Autowired(required = false)
    private PermissionCacheService permissionCacheService;
    
    @Autowired
    private UserPrivacyService privacyService;

    /**
     * 修改用户角色
     */
    @AuditLog(
        operation = "修改用户角色",
        resourceType = "USER",
        resourceId = "#userId"
    )
    @PutMapping("/users/{userId}/role")
    public ApiResponse<User> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String newRole,
            @RequestParam(required = false) String reason) {
        
        UserSession currentUser = UserContext.get();
        
        // 权限校验：超级管理员才能设置管理员角色
        if (!currentUser.isSuperAdmin() && 
            ("ADMIN".equals(newRole) || "SUPER_ADMIN".equals(newRole))) {
            throw new BusinessException("无权设置管理员角色，需要超级管理员权限");
        }
        
        // 查询目标用户
        User targetUser = userService.getById(userId);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        String oldRole = targetUser.getUserRole();
        
        // 更新角色
        targetUser.setUserRole(newRole);
        userService.updateById(targetUser);
        
        // 清除权限缓存（立即失效）
        if (permissionCacheService != null) {
            permissionCacheService.evictUserPermissions(userId);
        }
        
        log.info("用户角色已变更: operator={}, userId={}, oldRole={}, newRole={}, reason={}", 
            currentUser.getId(), userId, oldRole, newRole, reason);
        
        return ApiResponse.ok(targetUser);
    }

    /**
     * 强制用户下线（使Token失效）
     */
    @AuditLog(
        operation = "强制用户下线",
        resourceType = "USER"
    )
    @PostMapping("/users/{userId}/force-logout")
    public ApiResponse<Void> forceLogout(@PathVariable Long userId) {
        // 将用户Token加入黑名单
        // 实际实现需要在auth-service中添加相关逻辑
        log.info("强制用户下线: userId={}", userId);
        return ApiResponse.ok();
    }

    /**
     * 批量清除权限缓存
     */
    @PostMapping("/cache/evict")
    public ApiResponse<String> evictPermissionCache(
            @RequestParam(required = false) Long userId) {
        
        if (permissionCacheService == null) {
            return ApiResponse.ok("权限缓存服务未启用");
        }
        
        if (userId != null) {
            permissionCacheService.evictUserPermissions(userId);
            return ApiResponse.ok("已清除用户 " + userId + " 的权限缓存");
        } else {
            permissionCacheService.evictAll();
            return ApiResponse.ok("已清除所有权限缓存");
        }
    }

    /**
     * 获取用户隐私设置（管理员查看）
     */
    @GetMapping("/users/{userId}/privacy")
    public ApiResponse<UserPrivacySettings> getUserPrivacy(@PathVariable Long userId) {
        UserPrivacySettings settings = privacyService.getOrCreate(userId);
        return ApiResponse.ok(settings);
    }

    /**
     * 修改用户隐私设置（管理员修改）
     */
    @AuditLog(
        operation = "修改用户隐私设置",
        resourceType = "USER"
    )
    @PutMapping("/users/{userId}/privacy")
    public ApiResponse<Void> updateUserPrivacy(
            @PathVariable Long userId,
            @RequestBody UserPrivacySettings settings) {
        
        privacyService.updateSettings(userId, settings);
        return ApiResponse.ok();
    }
}
