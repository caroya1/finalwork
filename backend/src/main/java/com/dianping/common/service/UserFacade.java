package com.dianping.common.service;

import com.dianping.common.dto.UserSummary;
import com.dianping.common.dto.UserAuthView;
import com.dianping.user.service.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserFacade {
    private final UserService userService;

    public UserFacade(UserService userService) {
        this.userService = userService;
    }

    public UserAuthView findByLogin(String identity) {
        com.dianping.user.entity.User user = userService.findByLogin(identity);
        if (user == null) {
            return null;
        }
        return new UserAuthView(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getCity(),
                user.getUserRole(),
                user.getBalance()
        );
    }

    public UserSummary getSummary(Long userId) {
        return userService.getSummary(userId);
    }

    public void deductBalance(Long userId, BigDecimal amount) {
        userService.deductBalance(userId, amount);
    }

    public com.dianping.user.entity.User recharge(Long userId, BigDecimal amount) {
        return userService.recharge(userId, amount);
    }
}
