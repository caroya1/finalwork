package com.dianping.post.service;

import com.dianping.common.dto.AuditRequest;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.dto.ShopSummary;
import com.dianping.common.exception.BusinessException;
import com.dianping.common.port.AiPort;
import com.dianping.common.port.ShopPort;
import com.dianping.common.util.SensitiveWordFilter;
import com.dianping.post.client.UserClient;
import com.dianping.post.dto.PostCreateRequest;
import com.dianping.post.entity.Post;
import com.dianping.post.mapper.PostCommentMapper;
import com.dianping.post.mapper.PostMapper;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostLikeService postLikeService;

    @Mock
    private PostCommentMapper postCommentMapper;

    @Mock
    private ShopPort shopPort;

    @Mock
    private UserClient userClient;

    @Mock
    private Executor appTaskExecutor;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SensitiveWordFilter sensitiveWordFilter;

    @Mock
    private AiPort aiPort;

    private PostService postService;

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_TITLE = "测试帖子标题";
    private static final String TEST_CONTENT = "这是一个正常的帖子内容，用于测试AI审核功能。";
    private static final Long TEST_SHOP_ID = 1L;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(postMapper.insert(any(Post.class))).thenReturn(1);

        postService = new PostService(
            postMapper,
            postLikeService,
            postCommentMapper,
            shopPort,
            userClient,
            appTaskExecutor,
            redisTemplate,
            300L,
            sensitiveWordFilter,
            aiPort
        );
    }

    private ShopSummary createTestShopSummary() {
        ShopSummary shop = new ShopSummary();
        shop.setId(TEST_SHOP_ID);
        shop.setName("测试店铺");
        shop.setCity("北京");
        return shop;
    }

    @Test
    void createPost_WithApprovedContent_Success() {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle(TEST_TITLE);
        request.setContent(TEST_CONTENT);
        request.setShopId(TEST_SHOP_ID);
        request.setCity("北京");

        AuditResult approvedResult = new AuditResult(true, null, 0.95, "POST");

        when(sensitiveWordFilter.containsSensitiveWord(TEST_TITLE)).thenReturn(false);
        when(sensitiveWordFilter.containsSensitiveWord(TEST_CONTENT)).thenReturn(false);
        when(shopPort.getSummary(TEST_SHOP_ID)).thenReturn(createTestShopSummary());
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(approvedResult);

        Post result = postService.create(TEST_USER_ID, request);

        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(TEST_CONTENT, result.getContent());
        assertEquals(1, result.getAuditStatus(), "审核通过的帖子auditStatus应为1(APPROVED)");
        assertNull(result.getAuditRemark(), "审核通过的帖子auditRemark应为null");
        verify(postMapper).insert(any(Post.class));
        verify(aiPort).audit(any(AuditRequest.class));
    }

    @Test
    void createPost_WithRejectedContent_ThrowsException() {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle(TEST_TITLE);
        request.setContent("包含违规内容的信息");
        request.setShopId(TEST_SHOP_ID);

        AuditResult rejectedResult = new AuditResult(false, "包含不当内容", 0.85, "POST");

        when(sensitiveWordFilter.containsSensitiveWord(TEST_TITLE)).thenReturn(false);
        when(sensitiveWordFilter.containsSensitiveWord(request.getContent())).thenReturn(false);
        when(shopPort.getSummary(TEST_SHOP_ID)).thenReturn(createTestShopSummary());
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(rejectedResult);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.create(TEST_USER_ID, request);
        });

        assertTrue(exception.getMessage().contains("内容审核未通过"));
        verify(aiPort).audit(any(AuditRequest.class));
        verify(postMapper, never()).insert(any(Post.class));
    }

    @Test
    void createPost_AiServiceUnavailable_FallbackToSensitiveWordFilter() {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle(TEST_TITLE);
        request.setContent(TEST_CONTENT);
        request.setShopId(TEST_SHOP_ID);
        request.setCity("上海");

        FeignException feignException = createFeignException();

        lenient().when(sensitiveWordFilter.containsSensitiveWord(TEST_TITLE)).thenReturn(false);
        lenient().when(sensitiveWordFilter.containsSensitiveWord(TEST_CONTENT)).thenReturn(false);
        when(shopPort.getSummary(TEST_SHOP_ID)).thenReturn(createTestShopSummary());
        when(aiPort.audit(any(AuditRequest.class))).thenThrow(feignException);
        when(sensitiveWordFilter.containsSensitiveWord(anyString())).thenReturn(false);

        Post result = postService.create(TEST_USER_ID, request);

        assertNotNull(result);
        assertEquals(1, result.getAuditStatus(), "使用fallback审核通过后auditStatus应为1");
        verify(aiPort).audit(any(AuditRequest.class));
        verify(sensitiveWordFilter, atLeast(1)).containsSensitiveWord(anyString());
        verify(postMapper).insert(any(Post.class));
    }

    @Test
    void createPost_AiServiceUnavailable_WithSensitiveWords_Rejected() {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle(TEST_TITLE);
        request.setContent(TEST_CONTENT);
        request.setShopId(TEST_SHOP_ID);

        FeignException feignException = createFeignException();

        when(sensitiveWordFilter.containsSensitiveWord(anyString())).thenAnswer(invocation -> {
            String text = invocation.getArgument(0);
            // Pre-check: return false for individual title or content
            if (TEST_TITLE.equals(text) || TEST_CONTENT.equals(text)) {
                return false;
            }
            // Fallback check: return true for combined text (indicates sensitive word detected)
            if (text != null && text.contains(TEST_TITLE) && text.contains(TEST_CONTENT) && text.contains("\n")) {
                return true;
            }
            return false;
        });
        when(shopPort.getSummary(TEST_SHOP_ID)).thenReturn(createTestShopSummary());
        when(aiPort.audit(any(AuditRequest.class))).thenThrow(feignException);
        when(sensitiveWordFilter.getSensitiveWords(anyString())).thenReturn(Collections.singletonList("敏感词"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.create(TEST_USER_ID, request);
        });

        assertTrue(exception.getMessage().contains("包含敏感词"));
        verify(aiPort).audit(any(AuditRequest.class));
        verify(postMapper, never()).insert(any(Post.class));
    }

    @Test
    void createPost_AiReturnsNull_Rejected() {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle(TEST_TITLE);
        request.setContent(TEST_CONTENT);
        request.setShopId(TEST_SHOP_ID);

        when(sensitiveWordFilter.containsSensitiveWord(TEST_TITLE)).thenReturn(false);
        when(sensitiveWordFilter.containsSensitiveWord(TEST_CONTENT)).thenReturn(false);
        when(shopPort.getSummary(TEST_SHOP_ID)).thenReturn(createTestShopSummary());
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.create(TEST_USER_ID, request);
        });

        assertTrue(exception.getMessage().contains("内容审核未通过"));
        verify(aiPort).audit(any(AuditRequest.class));
        verify(postMapper, never()).insert(any(Post.class));
    }

    @Test
    void createPost_WithSensitiveWordsInTitle_ThrowsExceptionBeforeAiAudit() {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle("包含敏感词的标题");
        request.setContent(TEST_CONTENT);

        when(sensitiveWordFilter.containsSensitiveWord(request.getTitle())).thenReturn(true);
        when(sensitiveWordFilter.getSensitiveWords(request.getTitle())).thenReturn(Collections.singletonList("敏感词"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.create(TEST_USER_ID, request);
        });

        assertTrue(exception.getMessage().contains("标题包含敏感词"));
        verify(aiPort, never()).audit(any());
        verify(postMapper, never()).insert(any(Post.class));
    }

    @Test
    void createPost_WithSensitiveWordsInContent_ThrowsExceptionBeforeAiAudit() {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle(TEST_TITLE);
        request.setContent("包含敏感词的内容");

        when(sensitiveWordFilter.containsSensitiveWord(TEST_TITLE)).thenReturn(false);
        when(sensitiveWordFilter.containsSensitiveWord(request.getContent())).thenReturn(true);
        when(sensitiveWordFilter.getSensitiveWords(request.getContent())).thenReturn(Collections.singletonList("敏感词"));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.create(TEST_USER_ID, request);
        });

        assertTrue(exception.getMessage().contains("内容包含敏感词"));
        verify(aiPort, never()).audit(any());
        verify(postMapper, never()).insert(any(Post.class));
    }

    @Test
    void createPost_NullUserId_ThrowsException() {
        PostCreateRequest request = new PostCreateRequest();
        request.setTitle(TEST_TITLE);
        request.setContent(TEST_CONTENT);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.create(null, request);
        });

        assertEquals("userId is required", exception.getMessage());
        verify(postMapper, never()).insert(any(Post.class));
    }

    private FeignException createFeignException() {
        Request request = Request.create(
            Request.HttpMethod.POST,
            "/api/ai/audit",
            Collections.emptyMap(),
            null,
            StandardCharsets.UTF_8,
            new RequestTemplate()
        );
        return new FeignException.ServiceUnavailable("AI服务不可用", request, null, Collections.emptyMap());
    }
}
