package com.dianping.user.controller;

import com.dianping.common.dto.UserAuthView;
import com.dianping.common.dto.UserSummary;
import com.dianping.common.port.UserAuthPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/internal/users")
public class UserInternalController {
    private final UserAuthPort userAuthPort;

    public UserInternalController(UserAuthPort userAuthPort) {
        this.userAuthPort = userAuthPort;
    }

    @GetMapping("/auth")
    public UserAuthView findByLogin(@RequestParam("identity") String identity) {
        return userAuthPort.findByLogin(identity);
    }

    @GetMapping("/{id}/summary")
    public UserSummary getSummary(@PathVariable("id") Long userId) {
        return userAuthPort.getSummary(userId);
    }

    @GetMapping("/{id}/balance/deduct")
    public void deductBalance(@PathVariable("id") Long userId,
                              @RequestParam("amount") BigDecimal amount) {
        userAuthPort.deductBalance(userId, amount);
    }

    @GetMapping("/{id}/balance/recharge")
    public void recharge(@PathVariable("id") Long userId,
                         @RequestParam("amount") BigDecimal amount) {
        userAuthPort.recharge(userId, amount);
    }
}
