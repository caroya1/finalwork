package com.dianping.common.port;

public interface UserFollowPort {
    boolean isFollowing(Long followerId, Long followingId);
}
