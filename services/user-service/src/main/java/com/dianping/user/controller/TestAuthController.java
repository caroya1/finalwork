package com.dianping.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAuthController {

    @GetMapping("/test/auth")
    public String testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "No authentication in context";
        }
        return "Authenticated: " + auth.isAuthenticated() + 
               ", Principal: " + auth.getPrincipal() +
               ", Authorities: " + auth.getAuthorities();
    }
}
