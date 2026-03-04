package com.dianping.user.dto;

import javax.validation.constraints.NotBlank;

public class UpdateCityRequest {
    @NotBlank(message = "city is required")
    private String city;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
