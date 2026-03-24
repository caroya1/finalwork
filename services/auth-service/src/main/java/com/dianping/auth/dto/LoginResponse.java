package com.dianping.auth.dto;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String city;
    private String role;
    private java.math.BigDecimal balance;
    private String username;

    public LoginResponse(String token, String refreshToken, Long userId, String city, String role, java.math.BigDecimal balance, String username) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.city = city;
        this.role = role;
        this.balance = balance;
        this.username = username;
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

    public java.math.BigDecimal getBalance() {
        return balance;
    }

    public String getUsername() {
        return username;
    }
}
