package com.dianping.post.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.web.AuthUserResolver;
import com.dianping.post.dto.PostCreateRequest;
import com.dianping.post.dto.PostDetailResponse;
import com.dianping.post.entity.Post;
import com.dianping.post.dto.PostCommentRequest;
import com.dianping.post.service.PostCommentService;
import com.dianping.post.service.PostService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final PostCommentService postCommentService;

    public PostController(PostService postService, PostCommentService postCommentService) {
        this.postService = postService;
        this.postCommentService = postCommentService;
    }

    @GetMapping
    public ApiResponse<List<Post>> list(@RequestParam(value = "city", required = false) String city,
                                        @RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "shopId", required = false) Long shopId) {
        return ApiResponse.ok(postService.list(city, keyword, shopId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PostDetailResponse> detail(@PathVariable("id") Long id,
                                                  Authentication authentication,
                                                  @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        Long userId = AuthUserResolver.resolveUserId(authentication == null ? null : authentication.getPrincipal(), headerUserId);
        return ApiResponse.ok(postService.getDetail(id, userId));
    }

    @PostMapping("/{id}/comments")
    public ApiResponse<Void> comment(@PathVariable("id") Long id,
                                     @Valid @RequestBody PostCommentRequest request,
                                     Authentication authentication,
                                     @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        Long userId = AuthUserResolver.resolveUserId(authentication == null ? null : authentication.getPrincipal(), headerUserId);
        if (userId == null) {
            return ApiResponse.fail("login required");
        }
        postCommentService.addComment(id, userId, request);
        return ApiResponse.ok(null);
    }

    @PostMapping
    public ApiResponse<Post> create(@Valid @RequestBody PostCreateRequest request,
                                    Authentication authentication,
                                    @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        Long userId = AuthUserResolver.resolveUserId(authentication == null ? null : authentication.getPrincipal(), headerUserId);
        if (userId == null) {
            return ApiResponse.fail("login required");
        }
        return ApiResponse.ok(postService.create(userId, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id,
                                    Authentication authentication,
                                    @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        Long userId = AuthUserResolver.resolveUserId(authentication == null ? null : authentication.getPrincipal(), headerUserId);
        if (userId == null) {
            return ApiResponse.fail("login required");
        }
        postService.delete(id, userId);
        return ApiResponse.ok(null);
    }
}
