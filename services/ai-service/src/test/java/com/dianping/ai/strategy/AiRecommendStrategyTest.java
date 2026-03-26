package com.dianping.ai.strategy;

import com.dianping.ai.client.ShopClient;
import com.dianping.ai.util.IntentParser;
import com.dianping.ai.util.ParsedIntent;
import com.dianping.common.dto.ShopDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiRecommendStrategyTest {

    @Mock
    private IntentParser intentParser;

    @Mock
    private ShopClient shopClient;

    @InjectMocks
    private AiRecommendStrategy aiRecommendStrategy;

    private ShopDTO createShop(Long id, String name, String category, Double rating, String tags, String address) {
        ShopDTO shop = new ShopDTO();
        shop.setId(id);
        shop.setName(name);
        shop.setCategory(category);
        shop.setRating(rating);
        shop.setTags(tags);
        shop.setAddress(address);
        shop.setCity("广州");
        return shop;
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetName() {
        assertEquals("ai", aiRecommendStrategy.getName());
    }

    @Test
    void testIsSupported() {
        assertTrue(aiRecommendStrategy.isSupported(1L));
        assertTrue(aiRecommendStrategy.isSupported(null));
    }

    @Test
    void testRecommendWithQuery_MatchingShops() {
        ParsedIntent intent = new ParsedIntent("川菜", null, null, null, 0.25);
        when(intentParser.parse("想吃川菜")).thenReturn(intent);

        ShopDTO shop1 = createShop(1L, "川味馆", "川菜", 4.5, "麻辣,聚餐", "天河区");
        ShopDTO shop2 = createShop(2L, "粤菜轩", "粤菜", 4.0, "清淡,商务", "越秀区");
        ShopDTO shop3 = createShop(3L, "蜀香楼", "川菜", 4.8, "正宗,聚餐", "海珠区");
        when(shopClient.listByCity("广州")).thenReturn(Arrays.asList(shop1, shop2, shop3));

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("想吃川菜", "广州", 1L, 10);

        assertNotNull(result);
        assertTrue(result.size() <= 10);
        assertTrue(result.stream().anyMatch(s -> s.getCategory().contains("川菜")));
        verify(intentParser).parse("想吃川菜");
        verify(shopClient).listByCity("广州");
    }

    @Test
    void testRecommendWithQuery_MaxTenResults() {
        ParsedIntent intent = new ParsedIntent("川菜", null, null, null, 0.25);
        when(intentParser.parse("川菜")).thenReturn(intent);

        List<ShopDTO> manyShops = Arrays.asList(
                createShop(1L, "店1", "川菜", 4.5, null, "addr"),
                createShop(2L, "店2", "川菜", 4.3, null, "addr"),
                createShop(3L, "店3", "川菜", 4.1, null, "addr"),
                createShop(4L, "店4", "川菜", 4.0, null, "addr"),
                createShop(5L, "店5", "川菜", 3.9, null, "addr"),
                createShop(6L, "店6", "川菜", 3.8, null, "addr"),
                createShop(7L, "店7", "川菜", 3.7, null, "addr"),
                createShop(8L, "店8", "川菜", 3.6, null, "addr"),
                createShop(9L, "店9", "川菜", 3.5, null, "addr"),
                createShop(10L, "店10", "川菜", 3.4, null, "addr"),
                createShop(11L, "店11", "川菜", 3.3, null, "addr"),
                createShop(12L, "店12", "川菜", 3.2, null, "addr")
        );
        when(shopClient.listByCity("广州")).thenReturn(manyShops);

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("川菜", "广州", 1L, 15);

        assertTrue(result.size() <= 10);
    }

    @Test
    void testRecommendWithQuery_EmptyQuery() {
        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("", "广州", 1L, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(intentParser, never()).parse(anyString());
    }

    @Test
    void testRecommendWithQuery_NullQuery() {
        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery(null, "广州", 1L, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithQuery_EmptyCity() {
        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("川菜", "", 1L, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithQuery_NoPreferences_Fallback() {
        ParsedIntent emptyIntent = ParsedIntent.empty();
        when(intentParser.parse("随便")).thenReturn(emptyIntent);

        ShopDTO shop1 = createShop(1L, "店1", "川菜", 4.5, null, "addr");
        ShopDTO shop2 = createShop(2L, "店2", "粤菜", 4.8, null, "addr");
        when(shopClient.listByCity("广州")).thenReturn(Arrays.asList(shop1, shop2));

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("随便", "广州", 1L, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testRecommendWithQuery_NoMatchingShops_Fallback() {
        ParsedIntent intent = new ParsedIntent("泰国菜", null, null, null, 0.25);
        when(intentParser.parse("想吃泰国菜")).thenReturn(intent);

        ShopDTO shop1 = createShop(1L, "川味馆", "川菜", 4.5, null, "天河区");
        ShopDTO shop2 = createShop(2L, "粤菜轩", "粤菜", 4.0, null, "越秀区");
        when(shopClient.listByCity("广州")).thenReturn(Arrays.asList(shop1, shop2));

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("想吃泰国菜", "广州", 1L, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(4.5, result.get(0).getRating());
    }

    @Test
    void testRecommendWithQuery_SortedByRelevanceAndRating() {
        ParsedIntent intent = new ParsedIntent("川菜", null, null, null, 0.25);
        when(intentParser.parse("川菜")).thenReturn(intent);

        ShopDTO lowRating = createShop(1L, "川味馆", "川菜", 3.0, null, "addr");
        ShopDTO highRating = createShop(2L, "蜀香楼", "川菜", 4.8, null, "addr");
        ShopDTO mediumRating = createShop(3L, "巴蜀风", "川菜", 4.0, null, "addr");
        when(shopClient.listByCity("广州")).thenReturn(Arrays.asList(lowRating, highRating, mediumRating));

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("川菜", "广州", 1L, 10);

        assertNotNull(result);
        assertTrue(result.size() >= 2);
        assertTrue(result.get(0).getHotScore() >= result.get(result.size() - 1).getHotScore());
    }

    @Test
    void testRecommendWithQuery_ShopClientException() {
        ParsedIntent intent = new ParsedIntent("川菜", null, null, null, 0.25);
        when(intentParser.parse("川菜")).thenReturn(intent);
        when(shopClient.listByCity("广州")).thenThrow(new RuntimeException("Service unavailable"));

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("川菜", "广州", 1L, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithQuery_IntentParserException() {
        when(intentParser.parse("川菜")).thenThrow(new RuntimeException("Parser error"));

        ShopDTO shop1 = createShop(1L, "店1", "川菜", 4.5, null, "addr");
        when(shopClient.listByCity("广州")).thenReturn(Arrays.asList(shop1));

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("川菜", "广州", 1L, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testRecommendWithQuery_MatchScene() {
        ParsedIntent intent = new ParsedIntent(null, null, "约会", null, 0.25);
        when(intentParser.parse("适合约会的地方")).thenReturn(intent);

        ShopDTO romantic = createShop(1L, "浪漫西餐厅", "西餐", 4.5, "约会,浪漫,安静", "天河区");
        ShopDTO busy = createShop(2L, "热闹火锅", "川菜", 4.0, "聚餐,热闹", "越秀区");
        when(shopClient.listByCity("广州")).thenReturn(Arrays.asList(romantic, busy));

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("适合约会的地方", "广州", 1L, 10);

        assertNotNull(result);
        assertTrue(result.stream().anyMatch(s -> s.getName().contains("浪漫")));
    }

    @Test
    void testRecommendWithQuery_MatchLocation() {
        ParsedIntent intent = new ParsedIntent(null, null, null, "天河区", 0.25);
        when(intentParser.parse("天河区有什么好吃的")).thenReturn(intent);

        ShopDTO tianhe = createShop(1L, "天河美食", "粤菜", 4.5, null, "广州市天河区天河路");
        ShopDTO yuexiu = createShop(2L, "越秀小馆", "川菜", 4.0, null, "广州市越秀区中山路");
        when(shopClient.listByCity("广州")).thenReturn(Arrays.asList(tianhe, yuexiu));

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("天河区有什么好吃的", "广州", 1L, 10);

        assertNotNull(result);
        assertTrue(result.stream().anyMatch(s -> s.getAddress().contains("天河")));
    }

    @Test
    void testRecommendUsingThreadLocalQuery() {
        ParsedIntent intent = new ParsedIntent("川菜", null, null, null, 0.25);
        when(intentParser.parse("川菜")).thenReturn(intent);

        ShopDTO shop = createShop(1L, "川味馆", "川菜", 4.5, null, "addr");
        when(shopClient.listByCity("广州")).thenReturn(Arrays.asList(shop));

        aiRecommendStrategy.setQuery("川菜");
        List<ShopDTO> result = aiRecommendStrategy.recommend(1L, "广州", null, null, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testRecommendWithQuery_CustomSize() {
        ParsedIntent intent = new ParsedIntent("川菜", null, null, null, 0.25);
        when(intentParser.parse("川菜")).thenReturn(intent);

        ShopDTO shop1 = createShop(1L, "店1", "川菜", 4.5, null, "addr");
        ShopDTO shop2 = createShop(2L, "店2", "川菜", 4.3, null, "addr");
        ShopDTO shop3 = createShop(3L, "店3", "川菜", 4.1, null, "addr");
        when(shopClient.listByCity("广州")).thenReturn(Arrays.asList(shop1, shop2, shop3));

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("川菜", "广州", 1L, 2);

        assertEquals(2, result.size());
    }

    @Test
    void testRecommendWithQuery_EmptyCityShops() {
        ParsedIntent intent = new ParsedIntent("川菜", null, null, null, 0.25);
        when(intentParser.parse("川菜")).thenReturn(intent);
        when(shopClient.listByCity("深圳")).thenReturn(Collections.emptyList());

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("川菜", "深圳", 1L, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRecommendWithQuery_NullCityShops() {
        ParsedIntent intent = new ParsedIntent("川菜", null, null, null, 0.25);
        when(intentParser.parse("川菜")).thenReturn(intent);
        when(shopClient.listByCity("深圳")).thenReturn(null);

        List<ShopDTO> result = aiRecommendStrategy.recommendWithQuery("川菜", "深圳", 1L, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
