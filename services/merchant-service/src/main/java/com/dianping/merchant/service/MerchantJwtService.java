package com.dianping.merchant.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class MerchantJwtService {
    private static final String DEFAULT_SECRET = "dianping-merchant-jwt-secret-key-2026-safe";
    private final SecretKey secretKey;
    private final long accessExpireMinutes;
    private final long refreshExpireDays;

    public MerchantJwtService(@Value("${app.jwt.secret:}") String secret,
                               @Value("${app.jwt.expire-minutes:120}") long accessExpireMinutes,
                               @Value("${app.jwt.refresh-expire-days:7}") long refreshExpireDays) {
        String finalSecret = normalizeSecret(secret);
        this.secretKey = Keys.hmacShaKeyFor(finalSecret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireMinutes = accessExpireMinutes;
        this.refreshExpireDays = refreshExpireDays;
    }

    private String normalizeSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            return DEFAULT_SECRET;
        }
        if (secret.length() >= 32) {
            return secret;
        }
        StringBuilder sb = new StringBuilder(secret);
        while (sb.length() < 32) {
            sb.append('0');
        }
        return sb.toString();
    }

    public String generateAccessToken(Long merchantId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(merchantId))
                .claim("email", email)
                .claim("role", "merchant")
                .claim("type", "access")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessExpireMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long merchantId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(merchantId))
                .claim("email", email)
                .claim("type", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshExpireDays, ChronoUnit.DAYS)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(Long merchantId, String email) {
        return generateAccessToken(merchantId, email);
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long parseMerchantId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }
}
