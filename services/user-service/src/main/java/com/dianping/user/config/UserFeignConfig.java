package com.dianping.user.config;

import com.dianping.common.port.CouponPort;
import com.dianping.common.port.PasswordPort;
import com.dianping.common.port.PostPort;
import com.dianping.user.client.CouponClient;
import com.dianping.user.client.PasswordClient;
import com.dianping.user.client.PostClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserFeignConfig {
    @Bean
    public PostPort postPort(PostClient client) {
        return client;
    }

    @Bean
    public CouponPort couponPort(CouponClient client) {
        return client;
    }

    @Bean
    public PasswordPort passwordPort(PasswordClient client) {
        return client;
    }
}
