package com.dianping.user.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.user.service.UserFollowService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserFollowController {
    private final UserFollowService userFollowService;

    public UserFollowController(UserFollowService userFollowService) {
        this.userFollowService = userFollowService;
    }

    @PostMapping("/{id}/follow")
    public ApiResponse<Void> follow(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ApiResponse.fail("login required");
        }
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        userFollowService.follow(userId, id);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}/follow")
    public ApiResponse<Void> unfollow(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ApiResponse.fail("login required");
        }
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        userFollowService.unfollow(userId, id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/follow/status")
    public ApiResponse<Boolean> status(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ApiResponse.ok(false);
        }
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        return ApiResponse.ok(userFollowService.isFollowing(userId, id));
    }
}
