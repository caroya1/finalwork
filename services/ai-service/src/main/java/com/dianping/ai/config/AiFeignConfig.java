package com.dianping.ai.config;

import com.dianping.ai.client.ShopClient;
import com.dianping.common.port.ShopPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiFeignConfig {
    
    @Bean
    public ShopPort shopPort(ShopClient client) {
        return client;
    }
}
