package com.dianping.post.controller;

import com.dianping.common.dto.PostSummary;
import com.dianping.common.port.PostPort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/posts")
public class PostInternalController {
    private final PostPort postPort;

    public PostInternalController(PostPort postPort) {
        this.postPort = postPort;
    }

    @GetMapping("/summaries")
    public List<PostSummary> listSummaries(@RequestParam(value = "city", required = false) String city,
                                           @RequestParam(value = "keyword", required = false) String keyword,
                                           @RequestParam(value = "shopId", required = false) Long shopId) {
        return postPort.listSummaries(city, keyword, shopId);
    }

    @GetMapping("/user")
    public List<PostSummary> listSummariesByUser(@RequestParam("userId") Long userId) {
        return postPort.listSummariesByUser(userId);
    }
}
