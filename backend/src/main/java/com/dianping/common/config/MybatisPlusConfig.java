package com.dianping.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.dianping.**.mapper")
public class MybatisPlusConfig {
}
