package com.dianping.post.warmup;

import com.dianping.common.warmup.WarmupRunner;
import com.dianping.post.entity.Post;
import com.dianping.post.mapper.PostMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PostWarmupRunner extends WarmupRunner {
    
    private static final String POST_CACHE_PREFIX = "dp:post:";
    private static final int WARMUP_LIMIT = 30;
    
    private final PostMapper postMapper;
    
    public PostWarmupRunner(JdbcTemplate jdbcTemplate,
                            RedisTemplate<String, Object> redisTemplate,
                            PostMapper postMapper) {
        super("post-service", jdbcTemplate, redisTemplate);
        this.postMapper = postMapper;
    }
    
    @Override
    protected void warmupBusiness() {
        warmupHotPosts();
    }
    
    private void warmupHotPosts() {
        try {
            long start = System.currentTimeMillis();
            
            LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Post::getAuditStatus, 1)
                   .orderByDesc(Post::getLikes)
                   .last("LIMIT " + WARMUP_LIMIT);
            List<Post> hotPosts = postMapper.selectList(wrapper);
            
            if (hotPosts != null && !hotPosts.isEmpty()) {
                for (Post post : hotPosts) {
                    String cacheKey = POST_CACHE_PREFIX + post.getId();
                    redisTemplate.opsForValue().set(cacheKey, post, 30, TimeUnit.MINUTES);
                }
                logger.info("[post-service] 预热 {} 篇热门帖子缓存，耗时 {} ms",
                        hotPosts.size(), System.currentTimeMillis() - start);
            }
        } catch (Exception e) {
            logger.warn("[post-service] 热门帖子缓存预热失败: {}", e.getMessage());
        }
    }
}
