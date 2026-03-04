package com.dianping.user.dto;

import com.dianping.coupon.dto.UserCouponView;
import com.dianping.post.entity.Post;

import java.math.BigDecimal;
import java.util.List;

public class UserProfileResponse {
    private Long userId;
    private String username;
    private String role;
    private String city;
    private BigDecimal balance;
    private List<Post> posts;
    private List<UserCouponView> coupons;

    public UserProfileResponse(Long userId, String username, String role, String city,
                               BigDecimal balance, List<Post> posts, List<UserCouponView> coupons) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.city = city;
        this.balance = balance;
        this.posts = posts;
        this.coupons = coupons;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getCity() {
        return city;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public List<UserCouponView> getCoupons() {
        return coupons;
    }
}
