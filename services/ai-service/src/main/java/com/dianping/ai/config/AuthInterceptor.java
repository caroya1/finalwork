package com.dianping.ai.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (path.startsWith("/actuator/")) {
            return true;
        }

        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.isEmpty()) {
            logger.warn("未登录用户尝试访问AI服务: path={}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"请先登录后再使用AI助手功能\"}");
            return false;
        }

        logger.debug("AI服务访问: userId={}, path={}", userId, path);
        return true;
    }
}
