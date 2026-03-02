package com.dianping.post.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.post.entity.Post;
import com.dianping.post.service.PostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<List<Post>> list(@RequestParam(value = "city", required = false) String city,
                                        @RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.ok(postService.list(city, keyword));
    }
}
