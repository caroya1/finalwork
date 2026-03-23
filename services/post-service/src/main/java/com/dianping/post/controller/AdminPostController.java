package com.dianping.post.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.exception.BusinessException;
import com.dianping.post.entity.Post;
import com.dianping.post.mapper.PostMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/posts")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminPostController {
    private final PostMapper postMapper;

    public AdminPostController(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @GetMapping
    public ApiResponse<List<Post>> list(@RequestParam(required = false) Integer auditStatus) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(Post::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Post::getCreatedAt);
        return ApiResponse.ok(postMapper.selectList(wrapper));
    }

    @GetMapping("/{id}")
    public ApiResponse<Post> get(@PathVariable("id") Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        return ApiResponse.ok(post);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Post> approve(@PathVariable("id") Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        post.setAuditStatus(1);
        post.touchForUpdate();
        postMapper.updateById(post);
        return ApiResponse.ok(post);
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Post> reject(@PathVariable("id") Long id, @RequestParam(required = false) String reason) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        post.setAuditStatus(2);
        post.setAuditRemark(reason);
        post.touchForUpdate();
        postMapper.updateById(post);
        return ApiResponse.ok(post);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        postMapper.deleteById(id);
        return ApiResponse.ok(null);
    }
}
