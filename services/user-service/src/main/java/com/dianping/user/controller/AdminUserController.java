package com.dianping.user.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.exception.BusinessException;
import com.dianping.user.entity.User;
import com.dianping.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.user.mapper.UserMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminUserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public AdminUserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<List<User>> list(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return ApiResponse.ok(userMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public ApiResponse<User> get(@PathVariable("id") Long id) {
        User user = userService.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return ApiResponse.ok(user);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<User> updateStatus(@PathVariable("id") Long id, @RequestParam Integer status) {
        User user = userService.findById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        user.touchForUpdate();
        userMapper.updateById(user);
        return ApiResponse.ok(user);
    }

    @PutMapping("/{id}/disable")
    public ApiResponse<User> disable(@PathVariable("id") Long id) {
        return updateStatus(id, 0);
    }

    @PutMapping("/{id}/enable")
    public ApiResponse<User> enable(@PathVariable("id") Long id) {
        return updateStatus(id, 1);
    }
}
