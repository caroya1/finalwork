package com.dianping.auth.client;

import com.dianping.common.dto.UserAuthView;
import com.dianping.common.dto.UserSummary;
import com.dianping.common.port.UserAuthPort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserAuthClient extends UserAuthPort {
    @Override
    @GetMapping("/internal/users/auth")
    UserAuthView findByLogin(@RequestParam("identity") String identity);

    @Override
    @GetMapping("/internal/users/{id}/summary")
    UserSummary getSummary(@PathVariable("id") Long userId);
}
