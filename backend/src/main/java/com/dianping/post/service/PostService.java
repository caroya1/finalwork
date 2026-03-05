package com.dianping.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BusinessException;
import com.dianping.post.dto.PostCreateRequest;
import com.dianping.post.dto.PostDetailResponse;
import com.dianping.post.entity.Post;
import com.dianping.post.entity.PostComment;
import com.dianping.post.mapper.PostCommentMapper;
import com.dianping.post.mapper.PostMapper;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.mapper.ShopMapper;
import com.dianping.user.service.UserFollowService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class PostService {
    private final PostMapper postMapper;
    private final PostLikeService postLikeService;
    private final PostCommentMapper postCommentMapper;
    private final ShopMapper shopMapper;
    private final Executor appTaskExecutor;
    private final UserFollowService userFollowService;

    public PostService(PostMapper postMapper, PostLikeService postLikeService,
                       PostCommentMapper postCommentMapper, ShopMapper shopMapper,
                       @Qualifier("appTaskExecutor") Executor appTaskExecutor,
                       UserFollowService userFollowService) {
        this.postMapper = postMapper;
        this.postLikeService = postLikeService;
        this.postCommentMapper = postCommentMapper;
        this.shopMapper = shopMapper;
        this.appTaskExecutor = appTaskExecutor;
        this.userFollowService = userFollowService;
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
        Long authorId = post.getUserId();
        CompletableFuture<Long> likeCountFuture = CompletableFuture.supplyAsync(
                () -> postLikeService.countByPostId(postId), appTaskExecutor);
        CompletableFuture<Boolean> likedFuture = CompletableFuture.supplyAsync(
                () -> postLikeService.hasLiked(postId, userId), appTaskExecutor);
        CompletableFuture<Boolean> followedFuture = CompletableFuture.supplyAsync(() -> {
            if (userId == null || authorId == null) {
                return false;
            }
            return userFollowService.isFollowing(userId, authorId);
        }, appTaskExecutor);
        CompletableFuture<Shop> shopFuture = CompletableFuture.supplyAsync(() -> {
            if (post.getShopId() == null) {
                return null;
            }
            return shopMapper.selectById(post.getShopId());
        }, appTaskExecutor);
        CompletableFuture<List<PostComment>> commentsFuture = CompletableFuture.supplyAsync(() ->
                postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getPostId, postId)
                        .orderByDesc(PostComment::getCreatedAt)), appTaskExecutor);
        long likeCount = likeCountFuture.join();
        boolean liked = likedFuture.join();
        boolean followed = followedFuture.join();
        Shop shop = shopFuture.join();
        List<PostComment> comments = commentsFuture.join();
        return new PostDetailResponse(post, likeCount, liked, followed, shop, comments);
    }

    public Post create(Long userId, PostCreateRequest request) {
        if (userId == null) {
            throw new BusinessException("userId is required");
        }
        Shop shop = null;
        if (request.getShopId() != null) {
            shop = shopMapper.selectById(request.getShopId());
            if (shop == null) {
                throw new BusinessException("shop not found");
            }
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setShopId(request.getShopId());
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setCoverUrl(request.getCoverUrl());
        post.setCity(resolveCity(request.getCity(), shop));
        post.setTags(request.getTags());
        post.setLikes(0);
        post.touchForCreate();
        postMapper.insert(post);
        return post;
    }

    private String resolveCity(String city, Shop shop) {
        if (city != null && !city.trim().isEmpty()) {
            return city.trim();
        }
        if (shop != null && shop.getCity() != null && !shop.getCity().trim().isEmpty()) {
            return shop.getCity().trim();
        }
        return null;
    }
}
