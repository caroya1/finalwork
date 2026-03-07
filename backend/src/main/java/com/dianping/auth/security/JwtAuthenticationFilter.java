package com.dianping.auth.security;

import com.dianping.auth.service.JwtService;
import com.dianping.common.context.UserContext;
import com.dianping.common.context.UserSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    private static final String ACCESS_TOKEN_PREFIX = "dp:token:access:";
    private static final String REFRESH_TOKEN_PREFIX = "dp:token:refresh:";
    private static final String USER_ACCESS_PREFIX = "dp:token:user:access:";
    private static final String USER_REFRESH_PREFIX = "dp:token:user:refresh:";
    private static final String ACCESS_HEADER = "Authorization";
    private static final String REFRESH_HEADER = "X-Refresh-Token";

    public JwtAuthenticationFilter(JwtService jwtService,
                                   RedisTemplate<String, Object> redisTemplate,
                                   long accessTtlSeconds,
                                   long refreshTtlSeconds) {
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader(ACCESS_HEADER);
            String accessToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }
            UserSession session = resolveSession(accessToken, request, response);
            if (session != null) {
                UserContext.set(session);
            }
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private UserSession resolveSession(String accessToken,
                                       HttpServletRequest request, HttpServletResponse response) {
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            try {
                String tokenType = jwtService.getTokenType(accessToken);
                if ("access".equals(tokenType)) {
                    Object cached = redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + accessToken);
                    if (cached instanceof Map) {
                        return buildSession(cached, ACCESS_TOKEN_PREFIX + accessToken, accessTtlSeconds);
                    }
                }
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        String refreshHeader = request.getHeader(REFRESH_HEADER);
        if (refreshHeader == null || refreshHeader.trim().isEmpty()) {
            return null;
        }
        try {
            String tokenType = jwtService.getTokenType(refreshHeader);
            if (!"refresh".equals(tokenType)) {
                return null;
            }
            Object cached = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshHeader);
            if (!(cached instanceof Map)) {
                return null;
            }
            UserSession session = buildSession(cached, REFRESH_TOKEN_PREFIX + refreshHeader, refreshTtlSeconds);
            String newAccess = jwtService.generateAccessToken(session.getId(), session.getUsername());
            String newRefresh = jwtService.generateRefreshToken(session.getId(), session.getUsername());
            redisTemplate.opsForValue().set(ACCESS_TOKEN_PREFIX + newAccess, cached, accessTtlSeconds, java.util.concurrent.TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + newRefresh, cached, refreshTtlSeconds, java.util.concurrent.TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(USER_ACCESS_PREFIX + session.getId(), newAccess, refreshTtlSeconds, java.util.concurrent.TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(USER_REFRESH_PREFIX + session.getId(), newRefresh, refreshTtlSeconds, java.util.concurrent.TimeUnit.SECONDS);
            if (accessToken != null && !accessToken.trim().isEmpty()) {
                redisTemplate.delete(ACCESS_TOKEN_PREFIX + accessToken);
            }
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshHeader);
            response.setHeader(ACCESS_HEADER, "Bearer " + newAccess);
            response.setHeader(REFRESH_HEADER, newRefresh);
            return session;
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
            return null;
        }
    }

    private UserSession buildSession(Object cached, String key, long ttlSeconds) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) cached;
        Object userId = payload.get("userId");
        if (userId == null) {
            return null;
        }
        redisTemplate.expire(key, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
        Long id = Long.parseLong(String.valueOf(userId));
        String username = payload.get("username") == null ? null : String.valueOf(payload.get("username"));
        String role = payload.get("role") == null ? null : String.valueOf(payload.get("role"));
        String city = payload.get("city") == null ? null : String.valueOf(payload.get("city"));
        return new UserSession(id, username, role, city);
    }
}
