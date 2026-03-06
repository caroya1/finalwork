package com.dianping.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.exception.BusinessException;
import com.dianping.post.dto.PostCreateRequest;
import com.dianping.post.dto.PostDetailResponse;
import com.dianping.post.entity.Post;
import com.dianping.post.entity.PostComment;
import com.dianping.post.mapper.PostCommentMapper;
import com.dianping.post.mapper.PostMapper;
import com.dianping.common.dto.ShopSummary;
import com.dianping.common.dto.PostSummary;
import com.dianping.common.service.ShopFacade;
import com.dianping.common.service.UserFollowFacade;
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
    private final ShopFacade shopFacade;
    private final Executor appTaskExecutor;
    private final UserFollowFacade userFollowFacade;

    public PostService(PostMapper postMapper, PostLikeService postLikeService,
                       PostCommentMapper postCommentMapper, ShopFacade shopFacade,
                       @Qualifier("appTaskExecutor") Executor appTaskExecutor,
                       UserFollowFacade userFollowFacade) {
        this.postMapper = postMapper;
        this.postLikeService = postLikeService;
        this.postCommentMapper = postCommentMapper;
        this.shopFacade = shopFacade;
        this.appTaskExecutor = appTaskExecutor;
        this.userFollowFacade = userFollowFacade;
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

    public List<PostSummary> listSummaries(String city, String keyword, Long shopId) {
        List<Post> posts = list(city, keyword, shopId);
        List<PostSummary> result = new java.util.ArrayList<>();
        for (Post post : posts) {
            result.add(new PostSummary(
                    post.getId(),
                    post.getUserId(),
                    post.getShopId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getCoverUrl(),
                    post.getCity(),
                    post.getTags(),
                    post.getLikes(),
                    post.getCreatedAt()
            ));
        }
        return result;
    }

    public List<Post> listByUser(Long userId) {
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getUserId, userId)
                .orderByDesc(Post::getCreatedAt));
    }

    public List<PostSummary> listSummariesByUser(Long userId) {
        List<Post> posts = listByUser(userId);
        List<PostSummary> result = new java.util.ArrayList<>();
        for (Post post : posts) {
            result.add(new PostSummary(
                    post.getId(),
                    post.getUserId(),
                    post.getShopId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getCoverUrl(),
                    post.getCity(),
                    post.getTags(),
                    post.getLikes(),
                    post.getCreatedAt()
            ));
        }
        return result;
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
            return userFollowFacade.isFollowing(userId, authorId);
        }, appTaskExecutor);
        CompletableFuture<ShopSummary> shopFuture = CompletableFuture.supplyAsync(() -> {
            if (post.getShopId() == null) {
                return null;
            }
            return shopFacade.getSummary(post.getShopId());
        }, appTaskExecutor);
        CompletableFuture<List<PostComment>> commentsFuture = CompletableFuture.supplyAsync(() ->
                postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getPostId, postId)
                        .orderByDesc(PostComment::getCreatedAt)), appTaskExecutor);
        long likeCount = likeCountFuture.join();
        boolean liked = likedFuture.join();
        boolean followed = followedFuture.join();
        ShopSummary shop = shopFuture.join();
        List<PostComment> comments = commentsFuture.join();
        return new PostDetailResponse(post, likeCount, liked, followed, shop, comments);
    }

    public Post create(Long userId, PostCreateRequest request) {
        if (userId == null) {
            throw new BusinessException("userId is required");
        }
        if (request.getShopId() != null) {
            ShopSummary shop = shopFacade.getSummary(request.getShopId());
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
        post.setCity(resolveCity(request.getCity(), request.getShopId()));
        post.setTags(request.getTags());
        post.setLikes(0);
        post.touchForCreate();
        postMapper.insert(post);
        return post;
    }

    public void delete(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("post not found");
        }
        if (userId == null || post.getUserId() == null || !post.getUserId().equals(userId)) {
            throw new BusinessException("no permission");
        }
        postMapper.deleteById(postId);
    }

    private String resolveCity(String city, Long shopId) {
        if (city != null && !city.trim().isEmpty()) {
            return city.trim();
        }
        if (shopId != null) {
            ShopSummary shop = shopFacade.getSummary(shopId);
            if (shop != null && shop.getCity() != null && !shop.getCity().trim().isEmpty()) {
                return shop.getCity().trim();
            }
        }
        return null;
    }
}
