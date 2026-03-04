package com.dianping.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BusinessException;
import com.dianping.post.dto.PostDetailResponse;
import com.dianping.post.entity.Post;
import com.dianping.post.entity.PostComment;
import com.dianping.post.mapper.PostCommentMapper;
import com.dianping.post.mapper.PostMapper;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.mapper.ShopMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostMapper postMapper;
    private final PostLikeService postLikeService;
    private final PostCommentMapper postCommentMapper;
    private final ShopMapper shopMapper;

    public PostService(PostMapper postMapper, PostLikeService postLikeService,
                       PostCommentMapper postCommentMapper, ShopMapper shopMapper) {
        this.postMapper = postMapper;
        this.postLikeService = postLikeService;
        this.postCommentMapper = postCommentMapper;
        this.shopMapper = shopMapper;
    }

    public List<Post> list(String city, String keyword, Long shopId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (city != null && !city.trim().isEmpty()) {
            wrapper.eq(Post::getCity, city);
        }
        if (shopId != null) {
            wrapper.eq(Post::getShopId, shopId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(q -> q.like(Post::getTitle, keyword)
                    .or()
                    .like(Post::getContent, keyword));
        }
        wrapper.orderByDesc(Post::getCreatedAt);
        return postMapper.selectList(wrapper);
    }

    public List<Post> listByUser(Long userId) {
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreatedAt));
    }

    public PostDetailResponse getDetail(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("post not found");
        }
        long likeCount = postLikeService.countByPostId(postId);
        boolean liked = postLikeService.hasLiked(postId, userId);
        Shop shop = null;
        if (post.getShopId() != null) {
            shop = shopMapper.selectById(post.getShopId());
        }
        List<PostComment> comments = postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .orderByDesc(PostComment::getCreatedAt));
        return new PostDetailResponse(post, likeCount, liked, shop, comments);
    }
}
