package com.dianping.post.client;

import com.dianping.common.port.UserFollowPort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserFollowClient extends UserFollowPort {
    @Override
    @GetMapping("/internal/users/follow/status")
    boolean isFollowing(@RequestParam("followerId") Long followerId,
                        @RequestParam("followingId") Long followingId);
}
