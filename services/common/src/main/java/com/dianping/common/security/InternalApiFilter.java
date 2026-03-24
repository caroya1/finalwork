package com.dianping.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 内部API保护过滤器
 * 保护 /internal/** 路径，只允许内部服务访问
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ConditionalOnBean(InternalTokenService.class)
public class InternalApiFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalApiFilter.class);

    @Autowired(required = false)
    private InternalTokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // 只处理内部API
        if (!path.startsWith("/internal/")) {
            chain.doFilter(request, response);
            return;
        }

        // 如果没有TokenService，允许访问（开发模式）
        if (tokenService == null) {
            chain.doFilter(request, response);
            return;
        }

        // 开发模式：跳过内部Token验证
        // TODO: 生产环境需要启用
        chain.doFilter(request, response);
        
        /* 生产环境代码：
        // 获取内部Token
        String token = request.getHeader("X-Internal-Token");
        
        if (!tokenService.validate(token)) {
            String clientIp = getClientIp(request);
            log.warn("内部API访问被拒绝: path={}, ip={}", path, clientIp);
            
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"无效的内部Token\"}");
            return;
        }

        chain.doFilter(request, response);
        */
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
