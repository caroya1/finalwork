package com.dianping.recommendation.service;

import com.dianping.common.dto.ShopDTO;
import com.dianping.recommendation.dto.RecommendationRequest;
import com.dianping.common.dto.ShopSummary;
import com.dianping.recommendation.strategy.AiRecommendStrategy;
import com.dianping.recommendation.strategy.HotRecommendStrategy;
import com.dianping.recommendation.strategy.HybridRecommendStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @Mock
    private HybridRecommendStrategy hybridStrategy;
    
    @Mock
    private AiRecommendStrategy aiStrategy;
    
    @Mock
    private HotRecommendStrategy hotStrategy;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Executor testExecutor = Executors.newSingleThreadExecutor();
        
        recommendationService = new RecommendationService(
            null, null, redisTemplate, 300L, 
            testExecutor, hybridStrategy, aiStrategy, hotStrategy
        );
    }

    private ShopDTO createShopDTO(Long id, String name) {
        ShopDTO dto = new ShopDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCategory("川菜");
        dto.setCity("广州");
        dto.setRating(4.5);
        return dto;
    }

    @Test
    void testRecommend_DefaultStrategy() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(1L);
        request.setCity("广州");

        when(valueOperations.get(anyString())).thenReturn(null);
        when(hybridStrategy.recommend(any(), anyString(), any(), any(), any()))
            .thenReturn(Arrays.asList(createShopDTO(1L, "混合推荐店")));

        List<ShopSummary> result = recommendationService.recommend(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(hybridStrategy).recommend(any(), anyString(), any(), any(), any());
    }

    @Test
    void testRecommend_WithAiStrategy() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(1L);
        request.setCity("广州");
        request.setStrategy("ai");

        when(valueOperations.get(anyString())).thenReturn(null);
        when(aiStrategy.isSupported(any())).thenReturn(true);
        when(aiStrategy.recommend(any(), anyString(), any(), any(), any()))
            .thenReturn(Arrays.asList(createShopDTO(1L, "AI推荐店")));

        List<ShopSummary> result = recommendationService.recommend(request, "ai");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(aiStrategy).recommend(any(), anyString(), any(), any(), any());
    }

    @Test
    void testRecommend_WithHotStrategy() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(1L);
        request.setCity("广州");
        request.setStrategy("hot");

        when(valueOperations.get(anyString())).thenReturn(null);
        when(hotStrategy.isSupported(any())).thenReturn(true);
        when(hotStrategy.recommend(any(), anyString(), any(), any(), any()))
            .thenReturn(Arrays.asList(createShopDTO(1L, "热门推荐店")));

        List<ShopSummary> result = recommendationService.recommend(request, "hot");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(hotStrategy).recommend(any(), anyString(), any(), any(), any());
    }

    @Test
    void testRecommend_UnknownStrategy_FallsBackToDefault() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(1L);
        request.setCity("广州");
        request.setStrategy("unknown");

        when(valueOperations.get(anyString())).thenReturn(null);
        when(hybridStrategy.recommend(any(), anyString(), any(), any(), any()))
            .thenReturn(Arrays.asList(createShopDTO(1L, "默认推荐")));

        List<ShopSummary> result = recommendationService.recommend(request, "unknown");

        assertNotNull(result);
        verify(hybridStrategy).recommend(any(), anyString(), any(), any(), any());
    }

    @Test
    void testRecommend_UnsupportedStrategy_FallsBackToDefault() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(1L);
        request.setCity("广州");
        request.setStrategy("ai");

        when(valueOperations.get(anyString())).thenReturn(null);
        when(aiStrategy.isSupported(any())).thenReturn(false);
        when(hybridStrategy.recommend(any(), anyString(), any(), any(), any()))
            .thenReturn(Arrays.asList(createShopDTO(1L, "默认推荐")));

        List<ShopSummary> result = recommendationService.recommend(request, "ai");

        assertNotNull(result);
        verify(hybridStrategy).recommend(any(), anyString(), any(), any(), any());
    }

    @Test
    void testRecommend_CacheHit() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(1L);
        request.setCity("广州");
        request.setStrategy("ai");

        ShopSummary cached1 = new ShopSummary();
        cached1.setId(1L);
        cached1.setName("缓存店铺1");
        
        ShopSummary cached2 = new ShopSummary();
        cached2.setId(2L);
        cached2.setName("缓存店铺2");
        
        List<ShopSummary> cachedResults = Arrays.asList(cached1, cached2);
        when(valueOperations.get(anyString())).thenReturn(cachedResults);

        List<ShopSummary> result = recommendationService.recommend(request, "ai");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(aiStrategy, never()).recommend(any(), anyString(), any(), any(), any());
    }

    @Test
    void testStrategyCaseInsensitive() {
        RecommendationRequest request = new RecommendationRequest();
        request.setUserId(1L);
        request.setCity("广州");
        request.setStrategy("AI");

        when(valueOperations.get(anyString())).thenReturn(null);
        when(aiStrategy.isSupported(any())).thenReturn(true);
        when(aiStrategy.recommend(any(), anyString(), any(), any(), any()))
            .thenReturn(Arrays.asList(createShopDTO(1L, "AI店铺")));

        List<ShopSummary> result = recommendationService.recommend(request, "AI");

        assertNotNull(result);
        verify(aiStrategy).recommend(any(), anyString(), any(), any(), any());
    }
}
