package com.dianping.post.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.post.service.PostLikeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostLikeController {
    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Void> like(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ApiResponse.fail("login required");
        }
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        postLikeService.like(id, userId);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}/like")
    public ApiResponse<Void> unlike(@PathVariable("id") Long id, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ApiResponse.fail("login required");
        }
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        postLikeService.unlike(id, userId);
        return ApiResponse.ok(null);
    }
}
