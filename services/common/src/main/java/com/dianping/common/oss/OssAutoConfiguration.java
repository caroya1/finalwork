package com.dianping.common.oss;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * OSS自动配置类
 */
@Configuration
@EnableConfigurationProperties(TencentOssProperties.class)
@ComponentScan(basePackages = "com.dianping.common.oss")
public class OssAutoConfiguration {
}
