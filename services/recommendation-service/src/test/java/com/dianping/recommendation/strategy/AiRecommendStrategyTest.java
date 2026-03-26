package com.dianping.recommendation.strategy;

import com.dianping.common.dto.ShopDTO;
import com.dianping.common.port.AiPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiRecommendStrategyTest {

    @Mock
    private AiPort aiPort;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @Mock
    private HotRecommendStrategy hotRecommendStrategy;

    private AiRecommendStrategy aiRecommendStrategy;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        aiRecommendStrategy = new AiRecommendStrategy(aiPort, redisTemplate, hotRecommendStrategy);
        ReflectionTestUtils.setField(aiRecommendStrategy, "aiEnabled", true);
    }

    private ShopDTO createShop(Long id, String name, String category, Double rating) {
        ShopDTO shop = new ShopDTO();
        shop.setId(id);
        shop.setName(name);
        shop.setCategory(category);
        shop.setRating(rating);
        return shop;
    }

    @Test
    void testGetName() {
        assertEquals("ai", aiRecommendStrategy.getName());
    }

    @Test
    void testIsSupported_WhenEnabled_ReturnsTrue() {
        assertTrue(aiRecommendStrategy.isSupported(1L));
    }

    @Test
    void testRecommend_WithCacheHit() {
        List<ShopDTO> cachedShops = Arrays.asList(
            createShop(1L, "川味馆", "川菜", 4.5),
            createShop(2L, "粤菜轩", "粤菜", 4.0)
        );
        when(valueOperations.get(anyString())).thenReturn(cachedShops);

        List<ShopDTO> result = aiRecommendStrategy.recommend(1L, "广州", null, null, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(aiPort, never()).recommend(any());
    }

    @Test
    void testRecommend_WithCacheMiss_CallsAiPort() {
        when(valueOperations.get(anyString())).thenReturn(null);
        
        List<ShopDTO> aiShops = Arrays.asList(
            createShop(1L, "川味馆", "川菜", 4.5),
            createShop(2L, "蜀香楼", "川菜", 4.8)
        );
        when(aiPort.recommend(any())).thenReturn(aiShops);

        List<ShopDTO> result = aiRecommendStrategy.recommend(1L, "广州", null, null, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(aiPort).recommend(any());
    }

    @Test
    void testRecommend_WhenAiPortThrows_UsesFallback() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(aiPort.recommend(any())).thenThrow(new RuntimeException("AI service unavailable"));
        
        List<ShopDTO> fallbackShops = Arrays.asList(
            createShop(1L, "热门店", "川菜", 4.0)
        );
        when(hotRecommendStrategy.recommend(any(), anyString(), any(), any(), any())).thenReturn(fallbackShops);

        List<ShopDTO> result = aiRecommendStrategy.recommend(1L, "广州", null, null, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(hotRecommendStrategy).recommend(any(), anyString(), any(), any(), any());
    }

    @Test
    void testRecommend_WhenAiPortReturnsEmpty_UsesFallback() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(aiPort.recommend(any())).thenReturn(Collections.emptyList());
        
        List<ShopDTO> fallbackShops = Arrays.asList(
            createShop(1L, "热门店", "川菜", 4.0)
        );
        when(hotRecommendStrategy.recommend(any(), anyString(), any(), any(), any())).thenReturn(fallbackShops);

        List<ShopDTO> result = aiRecommendStrategy.recommend(1L, "广州", null, null, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testRecommend_RespectsSizeLimit() {
        when(valueOperations.get(anyString())).thenReturn(null);
        
        List<ShopDTO> manyShops = Arrays.asList(
            createShop(1L, "店1", "川菜", 4.5),
            createShop(2L, "店2", "川菜", 4.3),
            createShop(3L, "店3", "川菜", 4.1),
            createShop(4L, "店4", "川菜", 3.9),
            createShop(5L, "店5", "川菜", 3.7)
        );
        when(aiPort.recommend(any())).thenReturn(manyShops);

        List<ShopDTO> result = aiRecommendStrategy.recommend(1L, "广州", null, null, 3);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testIsAiServiceAvailable_WhenEnabled_ReturnsTrue() {
        when(aiPort.recommend(any())).thenReturn(Collections.emptyList());
        
        assertTrue(aiRecommendStrategy.isAiServiceAvailable());
    }

    @Test
    void testIsAiServiceAvailable_WhenThrows_ReturnsFalse() {
        when(aiPort.recommend(any())).thenThrow(new RuntimeException("Service down"));
        
        assertFalse(aiRecommendStrategy.isAiServiceAvailable());
    }
}