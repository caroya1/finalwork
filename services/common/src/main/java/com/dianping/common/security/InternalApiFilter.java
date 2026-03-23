package com.dianping.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class InternalApiFilter extends OncePerRequestFilter {

    @Autowired
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
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
