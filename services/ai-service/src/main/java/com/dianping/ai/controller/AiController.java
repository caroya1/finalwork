package com.dianping.ai.controller;

import com.dianping.ai.service.AiAuditService;
import com.dianping.ai.service.AiChatService;
import com.dianping.ai.service.AiRecommendationService;
import com.dianping.ai.service.ReasonGenerationService;
import com.dianping.ai.strategy.AiRecommendStrategy;
import com.dianping.common.api.ApiResponse;
import com.dianping.common.dto.AuditRequest;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.dto.GenerateReasonRequest;
import com.dianping.common.dto.GenerateReasonResponse;
import com.dianping.common.dto.RecommendRequest;
import com.dianping.common.dto.ShopDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final ReasonGenerationService reasonGenerationService;
    private final AiRecommendationService aiRecommendationService;
    private final AiAuditService aiAuditService;
    private final AiRecommendStrategy aiRecommendStrategy;
    private final AiChatService aiChatService;

    public AiController(ReasonGenerationService reasonGenerationService,
                        AiRecommendationService aiRecommendationService,
                        AiAuditService aiAuditService,
                        AiRecommendStrategy aiRecommendStrategy,
                        AiChatService aiChatService) {
        this.reasonGenerationService = reasonGenerationService;
        this.aiRecommendationService = aiRecommendationService;
        this.aiAuditService = aiAuditService;
        this.aiRecommendStrategy = aiRecommendStrategy;
        this.aiChatService = aiChatService;
    }

    @PostMapping("/recommend")
    public List<ShopDTO> recommend(@RequestBody RecommendRequest request) {
        logger.info("收到推荐请求: query={}, city={}", request.getQuery(), request.getCity());

        String city = request.getCity() != null ? request.getCity() : "上海";
        String query = request.getQuery() != null ? request.getQuery() : "";
        Long userId = request.getUserId();

        aiRecommendStrategy.setQuery(query);
        return aiRecommendStrategy.recommend(userId, city, null, null, 10);
    }

    @PostMapping("/chat")
    public ApiResponse<AiChatService.ChatRecommendationResult> chat(@RequestBody RecommendRequest request) {
        logger.info("收到AI聊天请求: query={}, city={}", request.getQuery(), request.getCity());

        String city = request.getCity() != null ? request.getCity() : "上海";
        String query = request.getQuery() != null ? request.getQuery() : "";
        Long userId = request.getUserId();

        AiChatService.ChatRecommendationResult result = aiChatService.recommend(query, city, userId);
        return ApiResponse.ok(result);
    }

    @PostMapping("/audit")
    public ApiResponse<AuditResult> audit(@RequestBody AuditRequest request) {
        logger.info("收到审核请求: type={}, targetId={}", request.getType(), request.getTargetId());
        AuditResult result = aiAuditService.audit(request.getContent(), request.getType());
        return ApiResponse.ok(result);
    }

    @PostMapping("/generate-reason")
    public ApiResponse<GenerateReasonResponse> generateReason(@RequestBody GenerateReasonRequest request) {
        GenerateReasonResponse response = reasonGenerationService.generateReason(request);
        return ApiResponse.ok(response);
    }
}
