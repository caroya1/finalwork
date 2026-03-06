package com.dianping.user.client;

import com.dianping.common.dto.PostSummary;
import com.dianping.common.port.PostPort;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "post-service")
public interface PostClient extends PostPort {
    @Override
    @GetMapping("/internal/posts/summaries")
    List<PostSummary> listSummaries(@RequestParam(value = "city", required = false) String city,
                                    @RequestParam(value = "keyword", required = false) String keyword,
                                    @RequestParam(value = "shopId", required = false) Long shopId);

    @Override
    @GetMapping("/internal/posts/user")
    List<PostSummary> listSummariesByUser(@RequestParam("userId") Long userId);
}
