package com.dianping.auth.service;

import com.dianping.common.port.PasswordPort;
import org.springframework.stereotype.Service;

@Service
public class PasswordPortImpl implements PasswordPort {
    private final PasswordService passwordService;

    public PasswordPortImpl(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @Override
    public String encode(String rawPassword) {
        return passwordService.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordService.matches(rawPassword, encodedPassword);
    }
}
