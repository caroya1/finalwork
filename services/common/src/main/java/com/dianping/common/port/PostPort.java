package com.dianping.common.port;

import com.dianping.common.dto.PostSummary;

import java.util.List;

public interface PostPort {
    List<PostSummary> listSummaries(String city, String keyword, Long shopId);

    List<PostSummary> listSummariesByUser(Long userId);
}
