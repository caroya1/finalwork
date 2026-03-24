package com.dianping.common.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 健康检查配置
 */
@Configuration
public class HealthCheckConfig {

    @Bean
    public HealthIndicator redisHealthIndicator() {
        return () -> {
            try {
                return Health.up().withDetail("redis", "OK").build();
            } catch (Exception e) {
                return Health.down().withDetail("redis", e.getMessage()).build();
            }
        };
    }
    
    @Bean
    public HealthIndicator dbHealthIndicator() {
        return () -> {
            try {
                return Health.up().withDetail("database", "OK").build();
            } catch (Exception e) {
                return Health.down().withDetail("database", e.getMessage()).build();
            }
        };
    }
}
