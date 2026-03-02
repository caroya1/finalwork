package com.dianping.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.post.entity.Post;
import com.dianping.post.mapper.PostMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostMapper postMapper;

    public PostService(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public List<Post> list(String city, String keyword) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (city != null && !city.trim().isEmpty()) {
            wrapper.eq(Post::getCity, city);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Post::getTitle, keyword).or().like(Post::getContent, keyword);
        }
        wrapper.orderByDesc(Post::getCreatedAt);
        return postMapper.selectList(wrapper);
    }
}
