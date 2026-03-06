package com.dianping.user.client;

import com.dianping.common.port.PasswordPort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface PasswordClient extends PasswordPort {
    @Override
    @PostMapping("/internal/password/encode")
    String encode(@RequestParam("rawPassword") String rawPassword);

    @Override
    @PostMapping("/internal/password/matches")
    boolean matches(@RequestParam("rawPassword") String rawPassword,
                    @RequestParam("encodedPassword") String encodedPassword);
}
