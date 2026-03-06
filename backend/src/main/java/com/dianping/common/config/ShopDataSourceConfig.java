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
@MapperScan(basePackages = "com.dianping.shop.mapper", sqlSessionFactoryRef = "shopSqlSessionFactory")
public class ShopDataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.shop")
    public DataSourceProperties shopDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource shopDataSource(@Qualifier("shopDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public SqlSessionFactory shopSqlSessionFactory(@Qualifier("shopDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(configuration);
        return bean.getObject();
    }

    @Bean
    public PlatformTransactionManager shopTransactionManager(@Qualifier("shopDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
