package com.dianping.auth.controller;

import com.dianping.common.port.PasswordPort;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/password")
public class PasswordInternalController {
    private final PasswordPort passwordPort;

    public PasswordInternalController(PasswordPort passwordPort) {
        this.passwordPort = passwordPort;
    }

    @PostMapping("/encode")
    public String encode(@RequestParam("rawPassword") String rawPassword) {
        return passwordPort.encode(rawPassword);
    }

    @PostMapping("/matches")
    public boolean matches(@RequestParam("rawPassword") String rawPassword,
                           @RequestParam("encodedPassword") String encodedPassword) {
        return passwordPort.matches(rawPassword, encodedPassword);
    }
}
