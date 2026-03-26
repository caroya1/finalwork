package com.dianping.recommendation.dto;

import java.util.List;

/**
 * AI智能推荐店铺响应DTO
 * 包含完整的店铺信息和推荐理由
 */
public class AIRecommendedShopDTO {
    
    private Long id;
    private String name;
    private String category;
    private String city;
    private Double rating;
    private String images;
    private String address;
    private Integer avgPrice;
    private String tags;
    private List<String> highlights;
    private String recommendReason;
    private Double hotScore;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Integer getAvgPrice() {
        return avgPrice;
    }
    
    public void setAvgPrice(Integer avgPrice) {
        this.avgPrice = avgPrice;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public List<String> getHighlights() {
        return highlights;
    }
    
    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }
    
    public String getRecommendReason() {
        return recommendReason;
    }
    
    public void setRecommendReason(String recommendReason) {
        this.recommendReason = recommendReason;
    }
    
    public Double getHotScore() {
        return hotScore;
    }
    
    public void setHotScore(Double hotScore) {
        this.hotScore = hotScore;
    }
}
