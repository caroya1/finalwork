package com.dianping.user.controller;

import com.dianping.common.dto.AdminAuthView;
import com.dianping.common.port.AdminAuthPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/admins")
public class AdminInternalController {
    private final AdminAuthPort adminAuthPort;

    public AdminInternalController(AdminAuthPort adminAuthPort) {
        this.adminAuthPort = adminAuthPort;
    }

    @GetMapping("/auth")
    public AdminAuthView findByUsername(@RequestParam("username") String username) {
        return adminAuthPort.findByUsername(username);
    }
}
