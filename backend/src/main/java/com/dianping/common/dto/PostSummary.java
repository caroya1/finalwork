package com.dianping.common.dto;

import java.time.LocalDateTime;

public class PostSummary {
    private Long id;
    private Long userId;
    private Long shopId;
    private String title;
    private String content;
    private String coverUrl;
    private String city;
    private String tags;
    private Integer likes;
    private LocalDateTime createdAt;

    public PostSummary() {
    }

    public PostSummary(Long id, Long userId, Long shopId, String title, String content,
                       String coverUrl, String city, String tags, Integer likes, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.shopId = shopId;
        this.title = title;
        this.content = content;
        this.coverUrl = coverUrl;
        this.city = city;
        this.tags = tags;
        this.likes = likes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
