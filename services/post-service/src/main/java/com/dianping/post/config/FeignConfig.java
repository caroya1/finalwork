package com.dianping.post.config;

import com.dianping.common.port.ShopPort;
import com.dianping.post.client.ShopClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public ShopPort shopPort(ShopClient client) {
        return client;
    }
}
