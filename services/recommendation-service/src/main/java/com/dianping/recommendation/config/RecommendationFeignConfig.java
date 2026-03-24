package com.dianping.recommendation.config;

import com.dianping.common.port.ShopPort;
import com.dianping.recommendation.client.ShopClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommendationFeignConfig {
    @Bean
    public ShopPort shopPort(ShopClient client) {
        return client;
    }
}
