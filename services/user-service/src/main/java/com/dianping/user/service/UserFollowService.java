package com.dianping.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BusinessException;
import com.dianping.user.entity.User;
import com.dianping.user.entity.UserFollow;
import com.dianping.user.mapper.UserFollowMapper;
import com.dianping.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserFollowService {
    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;

    public UserFollowService(UserFollowMapper userFollowMapper, UserMapper userMapper) {
        this.userFollowMapper = userFollowMapper;
        this.userMapper = userMapper;
    }

    public void follow(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            throw new BusinessException("invalid follow request");
        }
        if (followerId.equals(followingId)) {
            throw new BusinessException("cannot follow yourself");
        }
        User target = userMapper.selectById(followingId);
        if (target == null) {
            throw new BusinessException("user not found");
        }
        boolean exists = isFollowing(followerId, followingId);
        if (exists) {
            return;
        }
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        follow.setCreatedAt(LocalDateTime.now());
        userFollowMapper.insert(follow);
    }

    public void unfollow(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            throw new BusinessException("invalid follow request");
        }
        userFollowMapper.delete(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId));
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFollowingId, followingId)) > 0;
    }
}
