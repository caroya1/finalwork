package com.dianping.auth.client;

import com.dianping.common.dto.AdminAuthView;
import com.dianping.common.port.AdminAuthPort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", contextId = "adminAuthClient")
public interface AdminAuthClient extends AdminAuthPort {
    @Override
    @GetMapping("/internal/admins/auth")
    AdminAuthView findByUsername(@RequestParam("username") String username);
}
