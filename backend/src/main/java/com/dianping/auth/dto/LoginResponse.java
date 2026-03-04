package com.dianping.auth.dto;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String city;
    private String role;

    public LoginResponse(String token, String refreshToken, Long userId, String city, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.city = city;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCity() {
        return city;
    }

    public String getRole() {
        return role;
    }
}
