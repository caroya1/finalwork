package com.dianping.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostCommentService {
    private static final Logger log = LoggerFactory.getLogger(PostCommentService.class);

    public static final int AUDIT_STATUS_PENDING = 0;
    public static final int AUDIT_STATUS_APPROVED = 1;
    public static final int AUDIT_STATUS_REJECTED = 2;

    private final PostCommentMapper postCommentMapper;
    private final PostMapper postMapper;
    private final AiPort aiPort;
    private final SensitiveWordFilter sensitiveWordFilter;

    public PostCommentService(PostCommentMapper postCommentMapper, PostMapper postMapper,
                              AiPort aiPort, SensitiveWordFilter sensitiveWordFilter) {
        this.postCommentMapper = postCommentMapper;
        this.postMapper = postMapper;
        this.aiPort = aiPort;
        this.sensitiveWordFilter = sensitiveWordFilter;
    }

    public void addComment(Long postId, Long userId, PostCommentRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("post not found");
        }

        String content = request.getContent();
        AuditResult auditResult = performAudit(content, postId);

        if (auditResult != null && !Boolean.TRUE.equals(auditResult.getApproved())) {
            log.warn("Comment rejected by AI audit for postId: {}, userId: {}, reason: {}",
                    postId, userId, auditResult.getReason());
            throw new CommentRejectedException(auditResult.getReason());
        }

        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setAuditStatus(AUDIT_STATUS_APPROVED);

        postCommentMapper.insert(comment);
    }

    private AuditResult performAudit(String content, Long postId) {
        try {
            AuditRequest auditRequest = new AuditRequest(content, "COMMENT", postId);
            return aiPort.audit(auditRequest);
        } catch (Exception e) {
            log.warn("AI service unavailable for comment audit, falling back to SensitiveWordFilter. Error: {}",
                    e.getMessage());
            return performFallbackAudit(content);
        }
    }

    private AuditResult performFallbackAudit(String content) {
        if (sensitiveWordFilter.containsSensitiveWord(content)) {
            AuditResult result = new AuditResult();
            result.setApproved(false);
            result.setReason("Content contains sensitive words: " + sensitiveWordFilter.getSensitiveWords(content));
            result.setAuditType("SENSITIVE_WORD_FILTER");
            return result;
        }

        AuditResult result = new AuditResult();
        result.setApproved(true);
        result.setAuditType("SENSITIVE_WORD_FILTER");
        return result;
    }

    public static class CommentRejectedException extends BusinessException {
        public CommentRejectedException(String reason) {
            super("Comment rejected: " + reason);
        }
    }
}
