package com.dianping.recommendation.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.recommendation.dto.AIRecommendedShopDTO;
import com.dianping.recommendation.dto.RecommendationRequest;
import com.dianping.recommendation.service.AIAssistantService;
import com.dianping.recommendation.service.RecommendationService;
import com.dianping.common.dto.ShopSummary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@Validated
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final AIAssistantService aiAssistantService;

    public RecommendationController(RecommendationService recommendationService, 
                                    AIAssistantService aiAssistantService) {
        this.recommendationService = recommendationService;
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping
    public ApiResponse<List<ShopSummary>> recommend(@Valid @RequestBody RecommendationRequest request) {
        return ApiResponse.ok(recommendationService.recommend(request, request.getStrategy()));
    }
    
    /**
     * AI助手智能推荐接口
     * 为AI助手界面提供增强的推荐功能
     */
    @PostMapping("/ai-assistant")
    public ApiResponse<Map<String, Object>> aiAssistantRecommend(@Valid @RequestBody RecommendationRequest request) {
        List<AIRecommendedShopDTO> shops = aiAssistantService.recommendForAssistant(
            request.getUserId(), 
            request.getCity(), 
            request.getScene()
        );
        
        Map<String, Object> result = new HashMap<>();
        result.put("shops", shops);
        result.put("count", shops.size());
        
        return ApiResponse.ok(result);
    }
}
