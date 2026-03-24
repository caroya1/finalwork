package com.dianping.post.client;

import com.dianping.common.dto.UserSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", contextId = "userClient")
public interface UserClient {
    
    @GetMapping("/internal/users/{id}/summary")
    UserSummary getSummary(@PathVariable("id") Long userId);

    @GetMapping("/internal/users/follow/status")
    boolean isFollowing(@RequestParam("followerId") Long followerId,
                        @RequestParam("followingId") Long followingId);
}
