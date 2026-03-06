package com.dianping.common.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.dianping.coupon.mapper", sqlSessionFactoryRef = "couponSqlSessionFactory")
public class CouponDataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.coupon")
    public DataSourceProperties couponDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource couponDataSource(@Qualifier("couponDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public SqlSessionFactory couponSqlSessionFactory(@Qualifier("couponDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setConfiguration(new MybatisConfiguration());
        return bean.getObject();
    }

    @Bean
    public PlatformTransactionManager couponTransactionManager(@Qualifier("couponDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
