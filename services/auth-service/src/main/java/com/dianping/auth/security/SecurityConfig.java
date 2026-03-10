package com.dianping.auth.security;

import com.dianping.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtService jwtService,
                                           RedisTemplate<String, Object> redisTemplate,
                                           @Value("${app.jwt.expire-minutes:120}") long accessExpireMinutes,
                                           @Value("${app.jwt.refresh-expire-days:7}") long refreshExpireDays) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/users", "/api/users/*/city", "/api/users/*/recharge", "/api/users/*/profile").permitAll()
                        .requestMatchers("/api/auth/admin/login").permitAll()
                        .requestMatchers("/internal/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/*").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/shops", "/api/shops/*", "/api/shops/*/dishes").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/like", "/api/posts/*/comments", "/api/shops/*/rate", "/api/shops/*/dishes").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/*/like").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/coupons").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/merchants").permitAll()
                        .anyRequest().authenticated()
                );

        long accessTtlSeconds = accessExpireMinutes * 60;
        long refreshTtlSeconds = refreshExpireDays * 24 * 60 * 60;
        http.addFilterBefore(new JwtAuthenticationFilter(jwtService, redisTemplate, accessTtlSeconds, refreshTtlSeconds),
                UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(new UserContextAuthenticationFilter(), JwtAuthenticationFilter.class);
        return http.build();
    }
}
