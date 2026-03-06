package com.dianping.user.service;

import com.dianping.common.dto.UserAuthView;
import com.dianping.common.dto.UserSummary;
import com.dianping.common.port.UserAuthPort;
import org.springframework.context.annotation.Primary;
import com.dianping.user.entity.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Primary
public class UserAuthPortImpl implements UserAuthPort {
    private final UserService userService;

    public UserAuthPortImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserAuthView findByLogin(String identity) {
        User user = userService.findByLogin(identity);
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

    @Override
    public UserSummary getSummary(Long userId) {
        return userService.getSummary(userId);
    }

    @Override
    public void deductBalance(Long userId, BigDecimal amount) {
        userService.deductBalance(userId, amount);
    }

    @Override
    public void recharge(Long userId, BigDecimal amount) {
        userService.recharge(userId, amount);
    }
}
