package com.dianping.post.service;

import com.dianping.common.dto.PostSummary;
import com.dianping.common.port.PostPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostPortImpl implements PostPort {
    private final PostService postService;

    public PostPortImpl(PostService postService) {
        this.postService = postService;
    }

    @Override
    public List<PostSummary> listSummaries(String city, String keyword, Long shopId) {
        return postService.listSummaries(city, keyword, shopId);
    }

    @Override
    public List<PostSummary> listSummariesByUser(Long userId) {
        return postService.listSummariesByUser(userId);
    }
}
