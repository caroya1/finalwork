package com.dianping.common.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 敏感词过滤器（DFA算法）
 */
@Component
public class SensitiveWordFilter {

    /**
     * 敏感词树
     */
    private final Map<Character, Object> sensitiveWordMap = new HashMap<>();

    /**
     * 是否已初始化
     */
    private volatile boolean initialized = false;

    /**
     * 默认敏感词列表
     */
    private static final String[] DEFAULT_SENSITIVE_WORDS = {
        "操", "他妈", "妈的", "傻逼", "傻比", "蠢货", "废物", "垃圾", "混蛋", "滚蛋",
        "草泥马", "去死", "死全家", "不得好死", "王八蛋", "婊子", "贱人", "骚货",
        "fuck", "shit", "bitch", "ass", "damn", "crap", "hell", "stupid", "idiot"
    };

    @PostConstruct
    public void init() {
        loadSensitiveWords();
    }

    /**
     * 加载敏感词
     */
    private void loadSensitiveWords() {
        // 从配置文件加载（如果有）
        Set<String> words = loadFromFile();

        // 使用默认敏感词
        if (words.isEmpty()) {
            words.addAll(Arrays.asList(DEFAULT_SENSITIVE_WORDS));
        }

        // 构建敏感词树
        for (String word : words) {
            addWord(word.trim());
        }

        initialized = true;
        System.out.println("敏感词过滤器初始化完成，共加载 " + words.size() + " 个敏感词");
    }

    /**
     * 从配置文件加载敏感词
     */
    private Set<String> loadFromFile() {
        Set<String> words = new HashSet<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sensitive-words.txt")) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        words.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("加载敏感词文件失败: " + e.getMessage());
        }
        return words;
    }

    /**
     * 添加敏感词到树
     */
    private void addWord(String word) {
        if (word.isEmpty()) {
            return;
        }

        Map<Character, Object> currentMap = sensitiveWordMap;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Object obj = currentMap.get(c);
            if (obj == null) {
                Map<Character, Object> newMap = new HashMap<>();
                currentMap.put(c, newMap);
                currentMap = newMap;
            } else {
                currentMap = (Map<Character, Object>) obj;
            }

            // 最后一个字符标记为结束
            if (i == word.length() - 1) {
                currentMap.put('\0', true);
            }
        }
    }

    /**
     * 检查文本是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (!initialized || text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤敏感词，用*替换
     */
    public String filter(String text) {
        if (!initialized || text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        int index = 0;

        while (index < result.length()) {
            int length = checkSensitiveWord(result.toString(), index);
            if (length > 0) {
                // 替换敏感词为*
                for (int i = index; i < index + length && i < result.length(); i++) {
                    result.setCharAt(i, '*');
                }
                index += length;
            } else {
                index++;
            }
        }

        return result.toString();
    }

    /**
     * 获取文本中的敏感词列表
     */
    public List<String> getSensitiveWords(String text) {
        List<String> words = new ArrayList<>();
        if (!initialized || text == null || text.isEmpty()) {
            return words;
        }

        Set<String> foundWords = new HashSet<>();
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                String word = text.substring(i, i + length);
                if (foundWords.add(word)) {
                    words.add(word);
                }
                i += length - 1;
            }
        }
        return words;
    }

    /**
     * 检查从指定位置开始的敏感词长度
     */
    private int checkSensitiveWord(String text, int beginIndex) {
        int length = 0;
        Map<Character, Object> currentMap = sensitiveWordMap;
        boolean foundEnd = false;

        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            Object obj = currentMap.get(c);

            if (obj == null) {
                break;
            }

            currentMap = (Map<Character, Object>) obj;
            length++;

            // 检查是否是敏感词结尾
            if (currentMap.get('\0') != null) {
                foundEnd = true;
            }
        }

        return foundEnd ? length : 0;
    }
}
