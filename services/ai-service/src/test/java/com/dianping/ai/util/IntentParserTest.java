package com.dianping.ai.util;

import com.dianping.ai.service.DashScopeClient;
import com.dianping.ai.service.DashScopeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntentParserTest {

    @Mock
    private DashScopeClient dashScopeClient;

    private ObjectMapper objectMapper;
    private IntentParser intentParser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        intentParser = new IntentParser(dashScopeClient, objectMapper);
    }

    @Test
    void testParseWithCuisineOnly() throws Exception {
        String responseJson = "{\"cuisine\": \"川菜\", \"priceRange\": null, \"scene\": null, \"location\": null}";
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success(responseJson));

        ParsedIntent result = intentParser.parse("我想吃川菜");

        assertNotNull(result);
        assertEquals("川菜", result.getCuisine());
        assertNull(result.getPriceRange());
        assertNull(result.getScene());
        assertNull(result.getLocation());
        assertTrue(result.getConfidence() > 0);
        assertTrue(result.hasAnyPreference());
    }

    @Test
    void testParseWithAllFields() throws Exception {
        String responseJson = "{\"cuisine\": \"粤菜\", \"priceRange\": \"100-200\", \"scene\": \"商务宴请\", \"location\": \"天河区\"}";
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success(responseJson));

        ParsedIntent result = intentParser.parse("在天河区找一家粤菜，价位100-200左右，商务宴请用");

        assertNotNull(result);
        assertEquals("粤菜", result.getCuisine());
        assertEquals("100-200", result.getPriceRange());
        assertEquals("商务宴请", result.getScene());
        assertEquals("天河区", result.getLocation());
        assertEquals(1.0, result.getConfidence());
        assertTrue(result.hasAnyPreference());
    }

    @Test
    void testParseEmptyQuery() throws Exception {
        ParsedIntent result = intentParser.parse("");

        assertNotNull(result);
        assertFalse(result.hasAnyPreference());
        verify(dashScopeClient, never()).generate(anyString());
    }

    @Test
    void testParseNullQuery() throws Exception {
        ParsedIntent result = intentParser.parse(null);

        assertNotNull(result);
        assertFalse(result.hasAnyPreference());
        verify(dashScopeClient, never()).generate(anyString());
    }

    @Test
    void testParseWhitespaceQuery() throws Exception {
        ParsedIntent result = intentParser.parse("   ");

        assertNotNull(result);
        assertFalse(result.hasAnyPreference());
        verify(dashScopeClient, never()).generate(anyString());
    }

    @Test
    void testParseWithMarkdownJson() throws Exception {
        String responseWithMarkdown = "```json\n{\"cuisine\": \"西餐\", \"priceRange\": null, \"scene\": \"约会\", \"location\": null}\n```";
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success(responseWithMarkdown));

        ParsedIntent result = intentParser.parse("约会去吃西餐");

        assertNotNull(result);
        assertEquals("西餐", result.getCuisine());
        assertEquals("约会", result.getScene());
    }

    @Test
    void testParseApiFailure() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.failure("API error"));

        ParsedIntent result = intentParser.parse("我想吃火锅");

        assertNotNull(result);
        assertFalse(result.hasAnyPreference());
    }

    @Test
    void testParseException() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenThrow(new RuntimeException("Network error"));

        ParsedIntent result = intentParser.parse("找家餐厅");

        assertNotNull(result);
        assertFalse(result.hasAnyPreference());
    }

    @Test
    void testParseInvalidJson() throws Exception {
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success("This is not valid JSON"));

        ParsedIntent result = intentParser.parse("美食推荐");

        assertNotNull(result);
        assertFalse(result.hasAnyPreference());
    }

    @Test
    void testParseWithNullValues() throws Exception {
        String responseJson = "{\"cuisine\": null, \"priceRange\": null, \"scene\": null, \"location\": null}";
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success(responseJson));

        ParsedIntent result = intentParser.parse("随便吃点");

        assertNotNull(result);
        assertFalse(result.hasAnyPreference());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    void testConfidenceCalculation() throws Exception {
        String responseJson = "{\"cuisine\": \"日料\", \"priceRange\": null, \"scene\": null, \"location\": null}";
        when(dashScopeClient.generate(anyString()))
                .thenReturn(DashScopeResponse.success(responseJson));

        ParsedIntent result = intentParser.parse("日料");

        assertEquals(0.25, result.getConfidence());
    }

    @Test
    void testParsedIntentEmpty() {
        ParsedIntent empty = ParsedIntent.empty();

        assertNotNull(empty);
        assertFalse(empty.hasAnyPreference());
        assertEquals(0.0, empty.getConfidence());
    }

    @Test
    void testParsedIntentEqualsAndHashCode() {
        ParsedIntent intent1 = new ParsedIntent("川菜", "50-100", "聚餐", "天河", 0.75);
        ParsedIntent intent2 = new ParsedIntent("川菜", "50-100", "聚餐", "天河", 0.75);
        ParsedIntent intent3 = new ParsedIntent("粤菜", "50-100", "聚餐", "天河", 0.75);

        assertEquals(intent1, intent2);
        assertNotEquals(intent1, intent3);
        assertEquals(intent1.hashCode(), intent2.hashCode());
    }

    @Test
    void testParsedIntentToString() {
        ParsedIntent intent = new ParsedIntent("川菜", "50-100", "聚餐", "天河", 0.75);
        String str = intent.toString();

        assertTrue(str.contains("川菜"));
        assertTrue(str.contains("50-100"));
        assertTrue(str.contains("聚餐"));
        assertTrue(str.contains("天河"));
        assertTrue(str.contains("0.75"));
    }
}
