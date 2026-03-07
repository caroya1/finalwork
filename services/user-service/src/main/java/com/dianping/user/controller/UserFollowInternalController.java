package com.dianping.user.controller;

import com.dianping.common.port.UserFollowPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users/follow")
public class UserFollowInternalController {
    private final UserFollowPort userFollowPort;

    public UserFollowInternalController(UserFollowPort userFollowPort) {
        this.userFollowPort = userFollowPort;
    }

    @GetMapping("/status")
    public boolean isFollowing(@RequestParam("followerId") Long followerId,
                               @RequestParam("followingId") Long followingId) {
        return userFollowPort.isFollowing(followerId, followingId);
    }
}
