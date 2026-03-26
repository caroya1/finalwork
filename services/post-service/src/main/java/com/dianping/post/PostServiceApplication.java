package com.dianping.post;

import com.dianping.common.oss.OssAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.dianping")
@EnableFeignClients(basePackages = {"com.dianping.post.client", "com.dianping.common.port"})
@Import(OssAutoConfiguration.class)
public class PostServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostServiceApplication.class, args);
    }
}
