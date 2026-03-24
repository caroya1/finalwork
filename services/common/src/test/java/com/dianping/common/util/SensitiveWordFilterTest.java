package com.dianping.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveWordFilterTest {

    private SensitiveWordFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SensitiveWordFilter();
        filter.init();
    }

    @Test
    void testContainsSensitiveWord_EmptyText() {
        assertFalse(filter.containsSensitiveWord(""));
        assertFalse(filter.containsSensitiveWord(null));
    }

    @Test
    void testContainsSensitiveWord_NoSensitiveWords() {
        // 如果没有加载敏感词文件，默认列表为空
        boolean result = filter.containsSensitiveWord("这是一段正常文本");
        // 由于没有配置敏感词，应该返回false
        assertFalse(result);
    }

    @Test
    void testFilter_NormalText() {
        String text = "这是一段正常文本";
        String result = filter.filter(text);
        assertEquals(text, result);
    }

    @Test
    void testGetSensitiveWords_EmptyText() {
        List<String> words = filter.getSensitiveWords("");
        assertTrue(words.isEmpty());
        
        words = filter.getSensitiveWords(null);
        assertTrue(words.isEmpty());
    }

    @Test
    void testGetSensitiveWords_NormalText() {
        // 没有敏感词时返回空列表
        List<String> words = filter.getSensitiveWords("这是一段正常文本");
        assertTrue(words.isEmpty());
    }
}
