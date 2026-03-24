package com.dianping.common.port;

import com.dianping.common.dto.UserAuthView;
import com.dianping.common.dto.UserSummary;

import java.math.BigDecimal;

public interface UserAuthPort {
    UserAuthView findByLogin(String identity);

    UserSummary getSummary(Long userId);

    void recharge(Long userId, BigDecimal amount);

    void deductBalance(Long userId, BigDecimal amount);
}
