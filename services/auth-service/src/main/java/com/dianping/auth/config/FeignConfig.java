package com.dianping.auth.config;

import com.dianping.auth.client.UserAuthClient;
import com.dianping.common.port.UserAuthPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public UserAuthPort userAuthPort(UserAuthClient client) {
        return client;
    }
}
