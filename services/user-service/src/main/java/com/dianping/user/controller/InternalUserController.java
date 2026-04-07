package com.dianping.user.controller;

import com.dianping.user.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {
    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{userId}/deduct")
    public void deductBalance(@PathVariable("userId") Long userId, @RequestParam("amount") BigDecimal amount) {
        userService.deductBalance(userId, amount);
    }
}
