package com.dianping.recommendation.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class RecommendationRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "city is required")
    private String city;

    private String scene;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }
}
