package com.dianping.auth.service;

import com.dianping.auth.dto.LoginRequest;
import com.dianping.auth.dto.LoginResponse;
import com.dianping.auth.dto.TokenPairResponse;
import com.dianping.common.exception.BusinessException;
import com.dianping.user.entity.User;
import com.dianping.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private static final String ACCESS_TOKEN_PREFIX = "dp:token:access:";
    private static final String REFRESH_TOKEN_PREFIX = "dp:token:refresh:";
    private static final String USER_ACCESS_PREFIX = "dp:token:user:access:";
    private static final String USER_REFRESH_PREFIX = "dp:token:user:refresh:";

    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public AuthService(UserService userService,
                       PasswordService passwordService,
                       JwtService jwtService,
                       RedisTemplate<String, Object> redisTemplate,
                       @Value("${app.jwt.expire-minutes:120}") long accessExpireMinutes,
                       @Value("${app.jwt.refresh-expire-days:7}") long refreshExpireDays) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.accessTtlSeconds = accessExpireMinutes * 60;
        this.refreshTtlSeconds = refreshExpireDays * 24 * 60 * 60;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userService.findByLogin(request.getUsername());
        if (user == null) {
            throw new BusinessException("invalid username or password");
        }

        if (!passwordService.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("invalid username or password");
        }
        revokeExistingTokens(user.getId());
        String token = jwtService.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getUsername());
        saveTokens(user, token, refreshToken);
        return new LoginResponse(token, refreshToken, user.getId(), user.getCity(), user.getUserRole(), user.getBalance(), user.getUsername());
    }

    public TokenPairResponse refreshToken(String refreshToken) {
        Long userId;
        String tokenType;
        try {
            tokenType = jwtService.getTokenType(refreshToken);
            if (!"refresh".equals(tokenType)) {
                throw new BusinessException("invalid refresh token");
            }
            userId = jwtService.parseUserId(refreshToken);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("invalid refresh token");
        }
        String refreshKey = REFRESH_TOKEN_PREFIX + refreshToken;
        Object cached = redisTemplate.opsForValue().get(refreshKey);
        if (cached == null) {
            throw new BusinessException("refresh token expired");
        }
        String username = null;
        if (cached instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) cached;
            Object cachedUsername = payload.get("username");
            if (cachedUsername != null) {
                username = String.valueOf(cachedUsername);
            }
        }
        if (username == null || username.trim().isEmpty()) {
            User user = userService.findById(userId);
            if (user == null) {
                throw new BusinessException("user not found");
            }
            username = user.getUsername();
        }
        revokeExistingTokens(userId);
        String newAccess = jwtService.generateAccessToken(userId, username);
        String newRefresh = jwtService.generateRefreshToken(userId, username);
        if (cached instanceof Map) {
            redisTemplate.opsForValue().set(ACCESS_TOKEN_PREFIX + newAccess, cached, accessTtlSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + newRefresh, cached, refreshTtlSeconds, TimeUnit.SECONDS);
            storeTokenIndex(userId, newAccess, newRefresh);
        } else {
            User user = userService.findById(userId);
            if (user == null) {
                throw new BusinessException("user not found");
            }
            saveTokens(user, newAccess, newRefresh);
        }
        redisTemplate.delete(refreshKey);
        return new TokenPairResponse(newAccess, newRefresh);
    }

    public void logout(String accessToken, String refreshToken) {
        Long userId = null;
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            try {
                userId = jwtService.parseUserId(accessToken);
            } catch (Exception ignored) {
            }
        }
        if (userId == null && refreshToken != null && !refreshToken.trim().isEmpty()) {
            try {
                userId = jwtService.parseUserId(refreshToken);
            } catch (Exception ignored) {
            }
        }
        if (userId != null) {
            revokeExistingTokens(userId);
        }
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            redisTemplate.delete(ACCESS_TOKEN_PREFIX + accessToken);
        }
        if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
        }
        if (userId == null && refreshToken != null && !refreshToken.trim().isEmpty()) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
        }
    }

    private void saveTokens(User user, String accessToken, String refreshToken) {
        Map<String, String> payload = new HashMap<>(4);
        payload.put("userId", String.valueOf(user.getId()));
        payload.put("username", user.getUsername());
        payload.put("role", user.getUserRole());
        payload.put("city", user.getCity());
        redisTemplate.opsForValue().set(ACCESS_TOKEN_PREFIX + accessToken, payload, accessTtlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + refreshToken, payload, refreshTtlSeconds, TimeUnit.SECONDS);
        storeTokenIndex(user.getId(), accessToken, refreshToken);
    }

    private void storeTokenIndex(Long userId, String accessToken, String refreshToken) {
        redisTemplate.opsForValue().set(USER_ACCESS_PREFIX + userId, accessToken, refreshTtlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(USER_REFRESH_PREFIX + userId, refreshToken, refreshTtlSeconds, TimeUnit.SECONDS);
    }

    private void revokeExistingTokens(Long userId) {
        String accessKey = USER_ACCESS_PREFIX + userId;
        String refreshKey = USER_REFRESH_PREFIX + userId;
        String accessToken = asToken(redisTemplate.opsForValue().get(accessKey));
        String refreshToken = asToken(redisTemplate.opsForValue().get(refreshKey));
        if (accessToken != null && !accessToken.isEmpty()) {
            redisTemplate.delete(ACCESS_TOKEN_PREFIX + accessToken);
        }
        if (refreshToken != null && !refreshToken.isEmpty()) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
        }
        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);
    }

    private String asToken(Object value) {
        if (value == null) {
            return null;
        }
        String token = String.valueOf(value);
        return token.trim().isEmpty() ? null : token;
    }
}
