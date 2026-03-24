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
import com.dianping.common.port.ShopPort;
import com.dianping.common.port.UserFollowPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class PostService {
    private static final Logger log = LoggerFactory.getLogger(PostService.class);
    private static final String POST_CACHE_PREFIX = "dp:post:";

    private final PostMapper postMapper;
    private final PostLikeService postLikeService;
    private final PostCommentMapper postCommentMapper;
    private final ShopPort shopPort;
    private final Executor appTaskExecutor;
    private final UserFollowPort userFollowPort;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long cacheTtlSeconds;
    private final com.dianping.common.util.SensitiveWordFilter sensitiveWordFilter;

    public PostService(PostMapper postMapper, PostLikeService postLikeService,
                       PostCommentMapper postCommentMapper, ShopPort shopPort,
                       @Qualifier("appTaskExecutor") Executor appTaskExecutor,
                       UserFollowPort userFollowPort,
                       RedisTemplate<String, Object> redisTemplate,
                       @Value("${app.post.cache-ttl-seconds:300}") long cacheTtlSeconds,
                       com.dianping.common.util.SensitiveWordFilter sensitiveWordFilter) {
        this.postMapper = postMapper;
        this.postLikeService = postLikeService;
        this.postCommentMapper = postCommentMapper;
        this.shopPort = shopPort;
        this.appTaskExecutor = appTaskExecutor;
        this.userFollowPort = userFollowPort;
        this.redisTemplate = redisTemplate;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.sensitiveWordFilter = sensitiveWordFilter;
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
        Post post = getById(postId);
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
            return userFollowPort.isFollowing(userId, authorId);
        }, appTaskExecutor);
        CompletableFuture<ShopSummary> shopFuture = CompletableFuture.supplyAsync(() -> {
            if (post.getShopId() == null) {
                return null;
            }
            return shopPort.getSummary(post.getShopId());
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
        // 敏感词检测
        String title = request.getTitle().trim();
        String content = request.getContent().trim();
        if (sensitiveWordFilter.containsSensitiveWord(title)) {
            throw new BusinessException("标题包含敏感词: " + sensitiveWordFilter.getSensitiveWords(title));
        }
        if (sensitiveWordFilter.containsSensitiveWord(content)) {
            throw new BusinessException("内容包含敏感词: " + sensitiveWordFilter.getSensitiveWords(content));
        }
        if (request.getShopId() != null) {
            ShopSummary shop = shopPort.getSummary(request.getShopId());
            if (shop == null) {
                throw new BusinessException("shop not found");
            }
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setShopId(request.getShopId());
        post.setTitle(title);
        post.setContent(content);
        post.setCoverUrl(request.getCoverUrl());
        post.setCity(resolveCity(request.getCity(), request.getShopId()));
        post.setTags(request.getTags());
        post.setLikes(0);
        post.touchForCreate();
        postMapper.insert(post);
        safeSet(buildPostCacheKey(post.getId()), post, cacheTtlSeconds, TimeUnit.SECONDS);
        return post;
    }

    public void delete(Long postId, Long userId) {
        Post post = getById(postId);
        if (post == null) {
            throw new BusinessException("post not found");
        }
        if (userId == null || post.getUserId() == null || !post.getUserId().equals(userId)) {
            throw new BusinessException("no permission");
        }
        postMapper.deleteById(postId);
        safeDelete(buildPostCacheKey(postId));
    }

    private Post getById(Long postId) {
        if (postId == null) {
            return null;
        }
        String cacheKey = buildPostCacheKey(postId);
        Object cached = safeGet(cacheKey);
        if (cached instanceof Post) {
            return (Post) cached;
        }
        Post post = postMapper.selectById(postId);
        if (post != null) {
            safeSet(cacheKey, post, cacheTtlSeconds, TimeUnit.SECONDS);
        }
        return post;
    }

    private String buildPostCacheKey(Long postId) {
        return POST_CACHE_PREFIX + postId;
    }

    private Object safeGet(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RuntimeException ex) {
            log.warn("Redis get failed for key {}, fallback to database", key, ex);
            safeDelete(key);
            return null;
        }
    }

    private void safeSet(String key, Object value, long ttl, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, unit);
        } catch (RuntimeException ex) {
            log.warn("Redis set failed for key {}, skip cache write", key, ex);
        }
    }

    private void safeDelete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ignored) {
        }
    }

    private String resolveCity(String city, Long shopId) {
        if (city != null && !city.trim().isEmpty()) {
            return city.trim();
        }
        if (shopId != null) {
            ShopSummary shop = shopPort.getSummary(shopId);
            if (shop != null && shop.getCity() != null && !shop.getCity().trim().isEmpty()) {
                return shop.getCity().trim();
            }
        }
        return null;
    }
}
