package com.dianping.recommendation.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.recommendation.dto.RecommendationRequest;
import com.dianping.recommendation.service.RecommendationService;
import com.dianping.common.dto.ShopSummary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@Validated
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ApiResponse<List<ShopSummary>> recommend(@Valid @RequestBody RecommendationRequest request) {
        return ApiResponse.ok(recommendationService.recommend(request));
    }
}
