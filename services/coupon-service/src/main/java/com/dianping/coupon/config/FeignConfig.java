package com.dianping.coupon.config;

import com.dianping.common.port.UserAuthPort;
import com.dianping.coupon.client.UserClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public UserAuthPort userAuthPort(UserClient client) {
        return client;
    }
}
