package com.dianping.common.dto;

/**
 * AI生成推荐理由请求
 */
public class GenerateReasonRequest {
    
    private String shopName;
    private String category;
    private Double rating;
    private String tags;
    private String userQuery;
    private int rank;
    
    public GenerateReasonRequest() {
    }
    
    public GenerateReasonRequest(String shopName, String category, Double rating, String tags, String userQuery, int rank) {
        this.shopName = shopName;
        this.category = category;
        this.rating = rating;
        this.tags = tags;
        this.userQuery = userQuery;
        this.rank = rank;
    }
    
    public String getShopName() {
        return shopName;
    }
    
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getUserQuery() {
        return userQuery;
    }
    
    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }
    
    public int getRank() {
        return rank;
    }
    
    public void setRank(int rank) {
        this.rank = rank;
    }
}
