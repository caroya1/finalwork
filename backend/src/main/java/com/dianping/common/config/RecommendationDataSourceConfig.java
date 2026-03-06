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
@MapperScan(basePackages = "com.dianping.recommendation.mapper", sqlSessionFactoryRef = "recommendationSqlSessionFactory")
public class RecommendationDataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.recommendation")
    public DataSourceProperties recommendationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource recommendationDataSource(@Qualifier("recommendationDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public SqlSessionFactory recommendationSqlSessionFactory(@Qualifier("recommendationDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        bean.setConfiguration(configuration);
        return bean.getObject();
    }

    @Bean
    public PlatformTransactionManager recommendationTransactionManager(@Qualifier("recommendationDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
