package com.dianping.auth.client;

import com.dianping.common.dto.UserAuthView;
import com.dianping.common.dto.UserSummary;
import com.dianping.common.port.UserAuthPort;
import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", contextId = "userAuthClient")
public interface UserAuthClient extends UserAuthPort {
    @Override
    @GetMapping("/internal/users/auth")
    UserAuthView findByLogin(@RequestParam("identity") String identity);

    @Override
    @GetMapping("/internal/users/{id}/summary")
    UserSummary getSummary(@PathVariable("id") Long userId);

    @Override
    @GetMapping("/internal/users/{id}/balance/deduct")
    void deductBalance(@PathVariable("id") Long userId, @RequestParam("amount") BigDecimal amount);

    @Override
    @GetMapping("/internal/users/{id}/balance/recharge")
    void recharge(@PathVariable("id") Long userId, @RequestParam("amount") BigDecimal amount);
}
