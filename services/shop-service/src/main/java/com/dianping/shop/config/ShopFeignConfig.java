package com.dianping.shop.config;

import com.dianping.common.port.PostPort;
import com.dianping.shop.client.PostClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShopFeignConfig {
    @Bean
    public PostPort postPort(PostClient client) {
        return client;
    }
}
