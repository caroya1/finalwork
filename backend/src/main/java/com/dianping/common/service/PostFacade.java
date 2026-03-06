package com.dianping.common.service;

import com.dianping.common.dto.PostSummary;
import com.dianping.post.service.PostService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostFacade {
    private final PostService postService;

    public PostFacade(PostService postService) {
        this.postService = postService;
    }

    public List<PostSummary> listSummaries(String city, String keyword, Long shopId) {
        return postService.listSummaries(city, keyword, shopId);
    }

    public List<PostSummary> listSummariesByUser(Long userId) {
        return postService.listSummariesByUser(userId);
    }
}
