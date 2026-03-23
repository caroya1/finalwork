package com.dianping.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * JWT认证过滤器
 * 统一在网关层进行JWT验证
 */
@Component
@Slf4j
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.secret:DianpingJwtSecretKey2026}")
    private String jwtSecret;

    @Value("${gateway.white-list:/api/auth/login,/api/auth/register,/actuator/health,/api/public}")
    private String whiteListStr;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private List<String> whiteList;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 初始化白名单
        if (whiteList == null) {
            whiteList = Arrays.asList(whiteListStr.split(","));
        }

        // 1. 白名单放行
        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        // 2. 提取Token
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "缺少认证Token");
        }

        try {
            // 3. 验证并解析Token
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret.getBytes())
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);
            Long merchantId = claims.get("merchantId", Long.class);
            String username = claims.get("username", String.class);

            // 4. 检查Token黑名单（用户登出）
            String blackKey = "dp:token:blacklist:" + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blackKey))) {
                return unauthorized(exchange, "Token已失效");
            }

            // 5. 构建新的请求，添加用户Headers
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId != null ? userId.toString() : "")
                    .header("X-User-Role", role != null ? role : "")
                    .header("X-Merchant-Id", merchantId != null ? merchantId.toString() : "")
                    .header("X-Username", username != null ? username : "")
                    .header("X-Request-Id", UUID.randomUUID().toString())
                    .build();

            // 6. 记录访问日志（非查询操作）
            if (!isQueryMethod(request.getMethodValue())) {
                log.info("用户访问: userId={}, path={}, method={}", userId, path, request.getMethodValue());
            }

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", token.substring(0, Math.min(20, token.length())));
            return unauthorized(exchange, "Token已过期");
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return unauthorized(exchange, "Token无效");
        }
    }

    private boolean isQueryMethod(String method) {
        return "GET".equals(method) || "HEAD".equals(method);
    }

    private boolean isWhiteListed(String path) {
        return whiteList.stream().anyMatch(path::startsWith);
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"success\":false,\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
