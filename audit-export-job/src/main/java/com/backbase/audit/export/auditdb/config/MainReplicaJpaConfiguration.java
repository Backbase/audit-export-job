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
    entityManagerFactoryRef = AUDIT_ENTITY_MANAGER_FACTORY,
    transactionManagerRef = AUDIT_TRANSACTION_MANAGER,
    excludeFilters = @ComponentScan.Filter(ReadOnlyRepository.class)
)
public class MainReplicaJpaConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource.auditdb")
    public DataSourceProperties auditDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(AUDIT_DATA_SOURCE)
    public DataSource auditDataSource() {
        return auditDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(AUDIT_ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(
        @Qualifier(AUDIT_DATA_SOURCE) DataSource auditDataSource, EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(auditDataSource)
            .packages("com.backbase.audit.export.auditdb")
            .build();
    }

    @Bean(AUDIT_TRANSACTION_MANAGER)
    public PlatformTransactionManager auditTransactionManager(
        @Qualifier(AUDIT_ENTITY_MANAGER_FACTORY) LocalContainerEntityManagerFactoryBean auditEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(auditEntityManagerFactory.getObject()));
    }

}