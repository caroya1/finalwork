package com.dianping.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long accessExpireMinutes;
    private final long refreshExpireDays;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String TOKEN_REFRESH_PREFIX = "dp:token:refresh:";
    private static final long RENEWAL_THRESHOLD_MINUTES = 30; // 过期前30分钟自动续期

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expire-minutes:120}") long accessExpireMinutes,
                      @Value("${app.jwt.refresh-expire-days:7}") long refreshExpireDays,
                      RedisTemplate<String, Object> redisTemplate) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireMinutes = accessExpireMinutes;
        this.refreshExpireDays = refreshExpireDays;
        this.redisTemplate = redisTemplate;
    }

    public String generateAccessToken(Long userId, String username, String role) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role != null ? role : "USER")
                .claim("type", "access")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessExpireMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        
        // 将token存入Redis，用于续期检查
        String refreshKey = TOKEN_REFRESH_PREFIX + userId;
        redisTemplate.opsForValue().set(refreshKey, token, refreshExpireDays, TimeUnit.DAYS);
        
        return token;
    }

    public String generateRefreshToken(Long userId, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshExpireDays, ChronoUnit.DAYS)))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 检查并续期token
     * @return 如果需要续期返回新token，否则返回原token
     */
    public String checkAndRenewToken(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            // 如果token即将过期（在阈值内），则续期
            long timeUntilExpiry = expiration.getTime() - now.getTime();
            long thresholdMillis = RENEWAL_THRESHOLD_MINUTES * 60 * 1000;
            
            if (timeUntilExpiry > 0 && timeUntilExpiry < thresholdMillis) {
                Long userId = Long.parseLong(claims.getSubject());
                String username = claims.get("username", String.class);
                String tokenType = claims.get("type", String.class);
                
                // 检查Redis中是否有有效的刷新token
                String refreshKey = TOKEN_REFRESH_PREFIX + userId;
                Object storedToken = redisTemplate.opsForValue().get(refreshKey);
                
                if (storedToken != null && "access".equals(tokenType)) {
                    // 生成新的access token
                    String role = claims.get("role", String.class);
                    return generateAccessToken(userId, username, role);
                }
            }
            
            return token;
        } catch (ExpiredJwtException e) {
            // token已过期，返回null，由调用方处理
            return null;
        }
    }
    
    /**
     * 使token失效（登出时使用）
     */
    public void invalidateToken(Long userId) {
        String refreshKey = TOKEN_REFRESH_PREFIX + userId;
        redisTemplate.delete(refreshKey);
    }
    
    /**
     * 获取token剩余有效时间（分钟）
     */
    public long getRemainingTime(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining / (60 * 1000));
        } catch (ExpiredJwtException e) {
            return 0;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long parseUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getTokenType(String token) {
        Object type = parseClaims(token).get("type");
        return type == null ? null : String.valueOf(type);
    }
}
