package com.dianping.common.security;

import com.dianping.common.context.UserContext;
import com.dianping.common.context.UserSession;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 用户上下文过滤器
 * 从Gateway传递的Header中解析用户信息，设置到上下文
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        
        String requestId = request.getHeader("X-Request-Id");
        if (StringUtils.hasText(requestId)) {
            MDC.put("traceId", requestId);
        }
        
        try {
            String userId = request.getHeader("X-User-Id");
            String role = request.getHeader("X-User-Role");
            String merchantId = request.getHeader("X-Merchant-Id");
            String username = request.getHeader("X-Username");
            
            if (StringUtils.hasText(userId)) {
                UserSession session = new UserSession();
                session.setId(Long.valueOf(userId));
                session.setRole(role);
                session.setUsername(username);
                if (StringUtils.hasText(merchantId)) {
                    session.setMerchantId(Long.valueOf(merchantId));
                }
                
                // 设置到UserContext
                UserContext.set(session);
                
                // 设置到SecurityContext（用于@PreAuthorize）
                List<GrantedAuthority> authorities = 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    session, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                
                log.debug("用户上下文已设置: userId={}, role={}", userId, role);
            }
            
            chain.doFilter(request, response);
            
        } finally {
            // 清理上下文（防止内存泄漏）
            UserContext.clear();
            SecurityContextHolder.clearContext();
            MDC.clear();
        }
    }
}
