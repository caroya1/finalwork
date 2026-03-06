package com.dianping.user.service;

import com.dianping.common.port.UserFollowPort;
import org.springframework.stereotype.Service;

@Service
public class UserFollowPortImpl implements UserFollowPort {
    private final UserFollowService userFollowService;

    public UserFollowPortImpl(UserFollowService userFollowService) {
        this.userFollowService = userFollowService;
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowService.isFollowing(followerId, followingId);
    }
}
