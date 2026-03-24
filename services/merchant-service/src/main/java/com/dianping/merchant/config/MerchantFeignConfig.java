package com.dianping.merchant.config;

import com.dianping.common.port.PasswordPort;
import com.dianping.merchant.client.PasswordClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MerchantFeignConfig {
    @Bean
    public PasswordPort passwordPort(PasswordClient client) {
        return client;
    }
}
