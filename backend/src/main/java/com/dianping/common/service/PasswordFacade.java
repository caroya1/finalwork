package com.dianping.common.service;

import com.dianping.auth.service.PasswordService;
import org.springframework.stereotype.Service;

@Service
public class PasswordFacade {
    private final PasswordService passwordService;

    public PasswordFacade(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    public String encode(String rawPassword) {
        return passwordService.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordService.matches(rawPassword, encodedPassword);
    }
}
