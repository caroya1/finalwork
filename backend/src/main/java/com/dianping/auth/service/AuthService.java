package com.dianping.auth.service;

import com.dianping.auth.dto.LoginRequest;
import com.dianping.auth.dto.LoginResponse;
import com.dianping.common.exception.BusinessException;
import com.dianping.user.entity.User;
import com.dianping.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserService userService, PasswordService passwordService, JwtService jwtService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("invalid username or password");
        }

        if (!passwordService.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("invalid username or password");
        }
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId());
    }
}
