package com.backbase.audit.export.masterdb.config;

import com.backbase.audit.export.auditdb.config.ReadOnlyRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    basePackages = {"org.springframework.batch.core", "com.backbase.audit.export.masterdb"},
    entityManagerFactoryRef = ENTITY_MANAGER_FACTORY,
    transactionManagerRef = TRANSACTION_MANAGER,
    excludeFilters = @ComponentScan.Filter(ReadOnlyRepository.class)
)
public class MasterJpaConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("dataSource")
    @Primary
    public DataSource dataSource() {
        return dataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        @Qualifier("dataSource") DataSource dataSource, EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(dataSource)
            .packages("org.springframework.batch.core", "com.backbase.audit.export.masterdb")
            .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
        @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

}