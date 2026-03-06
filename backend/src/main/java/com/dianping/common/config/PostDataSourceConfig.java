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
@MapperScan(basePackages = "com.dianping.post.mapper", sqlSessionFactoryRef = "postSqlSessionFactory")
public class PostDataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.post")
    public DataSourceProperties postDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource postDataSource(@Qualifier("postDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean
    public SqlSessionFactory postSqlSessionFactory(@Qualifier("postDataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setConfiguration(new MybatisConfiguration());
        return bean.getObject();
    }

    @Bean
    public PlatformTransactionManager postTransactionManager(@Qualifier("postDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
