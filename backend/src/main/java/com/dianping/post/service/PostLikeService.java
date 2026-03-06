package com.dianping.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BusinessException;
import com.dianping.post.entity.Post;
import com.dianping.post.entity.PostLike;
import com.dianping.post.mapper.PostLikeMapper;
import com.dianping.post.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PostLikeService {
    private final PostLikeMapper postLikeMapper;
    private final PostMapper postMapper;

    public PostLikeService(PostLikeMapper postLikeMapper, PostMapper postMapper) {
        this.postLikeMapper = postLikeMapper;
        this.postMapper = postMapper;
    }

    public long countByPostId(Long postId) {
        return postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId));
    }

    public boolean hasLiked(Long postId, Long userId) {
        if (userId == null) {
            return false;
        }
        return postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId)) > 0;
    }

    @Transactional(transactionManager = "postTransactionManager")
    public void like(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("post not found");
        }
        boolean exists = hasLiked(postId, userId);
        if (exists) {
            throw new BusinessException("already liked");
        }
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        like.setCreatedAt(LocalDateTime.now());
        postLikeMapper.insert(like);

        long likeCount = countByPostId(postId);
        post.setLikes((int) likeCount);
        post.touchForUpdate();
        postMapper.updateById(post);
    }

    @Transactional(transactionManager = "postTransactionManager")
    public void unlike(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("post not found");
        }
        PostLike existing = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId));
        if (existing == null) {
            throw new BusinessException("not liked");
        }
        postLikeMapper.deleteById(existing.getId());

        long likeCount = countByPostId(postId);
        post.setLikes((int) likeCount);
        post.touchForUpdate();
        postMapper.updateById(post);
    }
}
