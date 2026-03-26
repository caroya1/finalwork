package com.dianping.common.dto;

import java.io.Serializable;

public class RecommendRequest implements Serializable {
    private String query;
    private String city;
    private Long userId;

    public RecommendRequest() {
    }

    public RecommendRequest(String query, String city, Long userId) {
        this.query = query;
        this.city = city;
        this.userId = userId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
