package com.backbase.audit.export.auditdb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;
import static com.backbase.audit.export.batch.constants.AuditJobConstants.*;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {"com.backbase.audit.export.auditdb"},
    entityManagerFactoryRef = READ_ENTITY_MANAGER_FACTORY,
    transactionManagerRef = READ_TRANSACTION_MANAGER,
    includeFilters = @ComponentScan.Filter(ReadOnlyRepository.class)
)
public class ReadReplicaJpaConfiguration {


    @Bean
    @ConfigurationProperties("spring.datasource.read-replica")
    public DataSourceProperties readDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(READ_DATA_SOURCE)
    public DataSource readDataSource() {
        return readDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(READ_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(
            @Qualifier(READ_DATA_SOURCE) DataSource readDataSource, EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(readDataSource)
            .packages("com.backbase.audit.export.auditdb")
            .build();
    }

    @Bean(READ_TRANSACTION_MANAGER)
    public PlatformTransactionManager readTransactionManager(
        @Qualifier(READ_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean readEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(readEntityManagerFactory.getObject()));
    }

}