package com.example.batch.shared.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String batchUrl;

    @Value("${spring.datasource.username}")
    private String batchUsername;

    @Value("${spring.datasource.password}")
    private String batchPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String batchDriverClassName;

    @Value("${business.datasource.url}")
    private String businessUrl;

    @Value("${business.datasource.username}")
    private String businessUsername;

    @Value("${business.datasource.password}")
    private String businessPassword;

    @Value("${business.datasource.driver-class-name}")
    private String businessDriverClassName;

    /**
     * Primary DataSource for Spring Batch metadata
     */
    @Primary
    @Bean(name = "batchDataSource")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create()
                .url(batchUrl)
                .username(batchUsername)
                .password(batchPassword)
                .driverClassName(batchDriverClassName)
                .build();
    }

    /**
     * Secondary DataSource for business data
     */
    @Bean(name = "businessDataSource")
    public DataSource businessDataSource() {
        return DataSourceBuilder.create()
                .url(businessUrl)
                .username(businessUsername)
                .password(businessPassword)
                .driverClassName(businessDriverClassName)
                .build();
    }

    /**
     * JdbcTemplate for batch operations (Spring Batch metadata)
     */
    @Primary
    @Bean(name = "batchJdbcTemplate")
    public JdbcTemplate batchJdbcTemplate(@Qualifier("batchDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * JdbcTemplate for business operations
     */
    @Bean(name = "businessJdbcTemplate")
    public JdbcTemplate businessJdbcTemplate(@Qualifier("businessDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * TransactionManager for Spring Batch metadata
     */
    @Primary
    @Bean(name = { "batchTransactionManager", "transactionManager" })
    public PlatformTransactionManager batchTransactionManager(@Qualifier("batchDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
