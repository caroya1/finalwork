package com.dianping.auth.dto;

public class TokenPairResponse {
    private String token;
    private String refreshToken;

    public TokenPairResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
