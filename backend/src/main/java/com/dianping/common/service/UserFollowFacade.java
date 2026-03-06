package com.dianping.common.service;

import com.dianping.user.service.UserFollowService;
import org.springframework.stereotype.Service;

@Service
public class UserFollowFacade {
    private final UserFollowService userFollowService;

    public UserFollowFacade(UserFollowService userFollowService) {
        this.userFollowService = userFollowService;
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowService.isFollowing(followerId, followingId);
    }
}
