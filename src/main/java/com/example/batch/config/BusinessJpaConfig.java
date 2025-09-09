package com.example.batch.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.batch.repository", entityManagerFactoryRef = "businessEntityManagerFactory", transactionManagerRef = "businessTransactionManager")
public class BusinessJpaConfig {

    @Bean(name = "businessEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean businessEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("businessDataSource") DataSource businessDataSource) {

        return builder
                .dataSource(businessDataSource)
                .packages("com.example.batch.model")
                .persistenceUnit("business")
                .build();
    }

    @Bean(name = "businessTransactionManager")
    public PlatformTransactionManager businessTransactionManager(
            @Qualifier("businessEntityManagerFactory") EntityManagerFactory businessEntityManagerFactory) {
        return new JpaTransactionManager(businessEntityManagerFactory);
    }
}
