package com.dianping.user.service;

import com.dianping.common.exception.BusinessException;
import com.dianping.auth.service.PasswordService;
import com.dianping.user.entity.User;
import com.dianping.user.dto.UserCreateRequest;
import com.dianping.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
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
        return userRepository.save(user);
    }

    public List<User> list() {
        return userRepository.findAll();
    }
}
