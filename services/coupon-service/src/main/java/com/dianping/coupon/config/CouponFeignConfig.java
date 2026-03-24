package com.dianping.coupon.config;

import com.dianping.common.port.UserAuthPort;
import com.dianping.coupon.client.UserAuthClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouponFeignConfig {
    @Bean
    public UserAuthPort userAuthPort(UserAuthClient client) {
        return client;
    }
}
