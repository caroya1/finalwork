package com.dianping.post.dto;

import com.dianping.common.dto.ShopSummary;
import com.dianping.post.entity.Post;
import java.util.List;

public class PostDetailResponse {
    private Post post;
    private String authorUsername;
    private long likeCount;
    private boolean liked;
    private boolean followed;
    private ShopSummary shop;
    private List<CommentDTO> comments;

    public PostDetailResponse(Post post, String authorUsername, long likeCount, boolean liked, boolean followed, ShopSummary shop, List<CommentDTO> comments) {
        this.post = post;
        this.authorUsername = authorUsername;
        this.likeCount = likeCount;
        this.liked = liked;
        this.followed = followed;
        this.shop = shop;
        this.comments = comments;
    }

    public Post getPost() {
        return post;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public boolean isFollowed() {
        return followed;
    }

    public ShopSummary getShop() {
        return shop;
    }

    public void setShop(ShopSummary shop) {
        this.shop = shop;
    }

    public List<CommentDTO> getComments() {
        return comments;
    }
}
