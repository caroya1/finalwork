package com.dianping.order.client;

import com.dianping.common.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/internal/users/{userId}/summary")
    UserSummary getSummary(@PathVariable("userId") Long userId);

    @PostMapping("/internal/users/{userId}/deduct")
    void deductBalance(@PathVariable("userId") Long userId, @RequestParam("amount") BigDecimal amount);
}
