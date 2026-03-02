package com.dianping.user.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.auth.service.PasswordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.user.entity.User;
import com.dianping.user.dto.UserCreateRequest;
import com.dianping.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final PasswordService passwordService;

    public UserService(UserMapper userMapper, PasswordService passwordService) {
        this.userMapper = userMapper;
        this.passwordService = passwordService;
    }

    public User create(UserCreateRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BusinessException("username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException("password is required");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordService.encode(request.getPassword()));
        user.touchForCreate();
        userMapper.insert(user);
        return user;
    }

    public List<User> list() {
        return userMapper.selectList(null);
    }

    public User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
}
