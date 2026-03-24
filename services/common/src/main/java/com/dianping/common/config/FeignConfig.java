package com.dianping.common.config;

import com.dianping.common.context.UserContext;
import com.dianping.common.context.UserSession;
import com.dianping.common.security.InternalTokenService;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Feign配置
 * 自动传递用户信息和内部Token
 */
@Configuration
public class FeignConfig {

    @Autowired(required = false)
    private InternalTokenService tokenService;

    /**
     * Feign请求拦截器
     * 自动添加用户上下文和内部Token
     */
    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return template -> {
            // 1. 传递用户信息
            UserSession session = UserContext.get();
            if (session != null) {
                template.header("X-User-Id", session.getId().toString());
                template.header("X-User-Role", session.getRole());
                template.header("X-Username", session.getUsername());
                if (session.getMerchantId() != null) {
                    template.header("X-Merchant-Id", session.getMerchantId().toString());
                }
            }

            // 2. 传递内部Token（如果调用的是内部API）
            if (tokenService != null) {
                String url = template.url();
                if (isInternalApi(url)) {
                    String token = tokenService.getCurrentToken();
                    if (StringUtils.hasText(token)) {
                        template.header("X-Internal-Token", token);
                    }
                }
            }
        };
    }

    private boolean isInternalApi(String url) {
        return url.contains("/internal/");
    }
}
