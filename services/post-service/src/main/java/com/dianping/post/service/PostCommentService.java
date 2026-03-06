package com.dianping.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BusinessException;
import com.dianping.post.dto.PostCommentRequest;
import com.dianping.post.entity.Post;
import com.dianping.post.entity.PostComment;
import com.dianping.post.mapper.PostCommentMapper;
import com.dianping.post.mapper.PostMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostCommentService {
    private final PostCommentMapper postCommentMapper;
    private final PostMapper postMapper;

    public PostCommentService(PostCommentMapper postCommentMapper, PostMapper postMapper) {
        this.postCommentMapper = postCommentMapper;
        this.postMapper = postMapper;
    }

    public void addComment(Long postId, Long userId, PostCommentRequest request) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("post not found");
        }
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        postCommentMapper.insert(comment);
    }
}
