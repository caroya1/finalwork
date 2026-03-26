package com.dianping.post.service;

import com.dianping.common.dto.AuditRequest;
import com.dianping.common.dto.AuditResult;
import com.dianping.common.exception.BusinessException;
import com.dianping.common.port.AiPort;
import com.dianping.common.util.SensitiveWordFilter;
import com.dianping.post.dto.PostCommentRequest;
import com.dianping.post.entity.Post;
import com.dianping.post.entity.PostComment;
import com.dianping.post.mapper.PostCommentMapper;
import com.dianping.post.mapper.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCommentServiceTest {

    @Mock
    private PostCommentMapper postCommentMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private AiPort aiPort;

    @Mock
    private SensitiveWordFilter sensitiveWordFilter;

    @InjectMocks
    private PostCommentService postCommentService;

    private static final Long POST_ID = 1L;
    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() {
    }

    @Test
    void addComment_ApprovedContent_SavesComment() {
        PostCommentRequest request = new PostCommentRequest();
        request.setContent("Great post!");

        Post post = new Post();
        post.setId(POST_ID);
        when(postMapper.selectById(POST_ID)).thenReturn(post);

        AuditResult auditResult = new AuditResult();
        auditResult.setApproved(true);
        auditResult.setReason(null);
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(auditResult);

        when(postCommentMapper.insert(any(PostComment.class))).thenReturn(1);

        postCommentService.addComment(POST_ID, USER_ID, request);

        ArgumentCaptor<PostComment> commentCaptor = ArgumentCaptor.forClass(PostComment.class);
        verify(postCommentMapper).insert(commentCaptor.capture());
        PostComment savedComment = commentCaptor.getValue();
        assertEquals(POST_ID, savedComment.getPostId());
        assertEquals(USER_ID, savedComment.getUserId());
        assertEquals("Great post!", savedComment.getContent());
        assertEquals(PostCommentService.AUDIT_STATUS_APPROVED, savedComment.getAuditStatus());
    }

    @Test
    void addComment_RejectedContent_ThrowsException() {
        PostCommentRequest request = new PostCommentRequest();
        request.setContent("Inappropriate content");

        Post post = new Post();
        post.setId(POST_ID);
        when(postMapper.selectById(POST_ID)).thenReturn(post);

        AuditResult auditResult = new AuditResult();
        auditResult.setApproved(false);
        auditResult.setReason("Contains inappropriate language");
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(auditResult);

        PostCommentService.CommentRejectedException exception = assertThrows(
                PostCommentService.CommentRejectedException.class,
                () -> postCommentService.addComment(POST_ID, USER_ID, request)
        );

        assertTrue(exception.getMessage().contains("Contains inappropriate language"));
        verify(postCommentMapper, never()).insert(any(PostComment.class));
    }

    @Test
    void addComment_AiServiceUnavailable_FallsBackToSensitiveWordFilter() {
        PostCommentRequest request = new PostCommentRequest();
        request.setContent("Normal content");

        Post post = new Post();
        post.setId(POST_ID);
        when(postMapper.selectById(POST_ID)).thenReturn(post);

        when(aiPort.audit(any(AuditRequest.class))).thenThrow(new RuntimeException("AI service unavailable"));
        when(sensitiveWordFilter.containsSensitiveWord("Normal content")).thenReturn(false);
        when(postCommentMapper.insert(any(PostComment.class))).thenReturn(1);

        postCommentService.addComment(POST_ID, USER_ID, request);

        verify(sensitiveWordFilter).containsSensitiveWord("Normal content");
        verify(postCommentMapper).insert(any(PostComment.class));
    }

    @Test
    void addComment_AiServiceUnavailable_ContainsSensitiveWords_ThrowsException() {
        PostCommentRequest request = new PostCommentRequest();
        request.setContent("Bad words here");

        Post post = new Post();
        post.setId(POST_ID);
        when(postMapper.selectById(POST_ID)).thenReturn(post);

        when(aiPort.audit(any(AuditRequest.class))).thenThrow(new RuntimeException("AI service unavailable"));
        when(sensitiveWordFilter.containsSensitiveWord("Bad words here")).thenReturn(true);
        when(sensitiveWordFilter.getSensitiveWords("Bad words here")).thenReturn(java.util.Arrays.asList("Bad"));

        PostCommentService.CommentRejectedException exception = assertThrows(
                PostCommentService.CommentRejectedException.class,
                () -> postCommentService.addComment(POST_ID, USER_ID, request)
        );

        assertTrue(exception.getMessage().contains("Content contains sensitive words"));
        verify(postCommentMapper, never()).insert(any(PostComment.class));
    }

    @Test
    void addComment_PostNotFound_ThrowsException() {
        PostCommentRequest request = new PostCommentRequest();
        request.setContent("Great post!");

        when(postMapper.selectById(POST_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> postCommentService.addComment(POST_ID, USER_ID, request)
        );

        assertEquals("post not found", exception.getMessage());
        verify(postCommentMapper, never()).insert(any(PostComment.class));
    }

    @Test
    void addComment_VerifiesAuditRequestType() {
        PostCommentRequest request = new PostCommentRequest();
        request.setContent("Test content");

        Post post = new Post();
        post.setId(POST_ID);
        when(postMapper.selectById(POST_ID)).thenReturn(post);

        AuditResult auditResult = new AuditResult();
        auditResult.setApproved(true);
        when(aiPort.audit(any(AuditRequest.class))).thenReturn(auditResult);
        when(postCommentMapper.insert(any(PostComment.class))).thenReturn(1);

        postCommentService.addComment(POST_ID, USER_ID, request);

        ArgumentCaptor<AuditRequest> auditRequestCaptor = ArgumentCaptor.forClass(AuditRequest.class);
        verify(aiPort).audit(auditRequestCaptor.capture());
        AuditRequest capturedRequest = auditRequestCaptor.getValue();
        assertEquals("COMMENT", capturedRequest.getType());
        assertEquals("Test content", capturedRequest.getContent());
        assertEquals(POST_ID, capturedRequest.getTargetId());
    }
}
