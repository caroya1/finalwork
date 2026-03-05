package com.dianping.post.dto;

import com.dianping.post.entity.Post;
import com.dianping.post.entity.PostComment;
import com.dianping.shop.entity.Shop;

import java.util.List;

public class PostDetailResponse {
    private Post post;
    private long likeCount;
    private boolean liked;
    private boolean followed;
    private Shop shop;
    private List<PostComment> comments;

    public PostDetailResponse(Post post, long likeCount, boolean liked, boolean followed, Shop shop, List<PostComment> comments) {
        this.post = post;
        this.likeCount = likeCount;
        this.liked = liked;
        this.followed = followed;
        this.shop = shop;
        this.comments = comments;
    }

    public Post getPost() {
        return post;
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

    public Shop getShop() {
        return shop;
    }

    public List<PostComment> getComments() {
        return comments;
    }
}
