package com.dianping.post.client;

import com.dianping.common.dto.UserSummary;
import com.dianping.common.port.UserPort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient extends UserPort {
    @Override
    @GetMapping("/internal/users/{id}/summary")
    UserSummary getSummary(@PathVariable("id") Long userId);
}
