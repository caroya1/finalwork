package com.dianping.user.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.auth.service.PasswordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.user.entity.User;
import com.dianping.user.dto.UserCreateRequest;
import com.dianping.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        user.setCity(request.getCity());
        user.setUserRole(request.getRole() == null || request.getRole().trim().isEmpty() ? "user" : request.getRole());
        user.setBalance(BigDecimal.ZERO);
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

    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }

    public User updateCity(Long userId, String city) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        user.setCity(city);
        user.touchForUpdate();
        userMapper.updateById(user);
        return user;
    }

    public User recharge(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("amount must be positive");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        BigDecimal current = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        user.setBalance(current.add(amount));
        user.touchForUpdate();
        userMapper.updateById(user);
        return user;
    }

    public void deductBalance(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("amount must be positive");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        BigDecimal current = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        if (current.compareTo(amount) < 0) {
            throw new BusinessException("insufficient balance");
        }
        user.setBalance(current.subtract(amount));
        user.touchForUpdate();
        userMapper.updateById(user);
    }
}
