package com.backbase.audit.export.batch.reader;

import com.backbase.audit.export.auditdb.entity.AuditMessage;
import com.backbase.audit.export.config.JobConfigurationProperties;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.Map;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuditExportReaderConfig {
    private final JobConfigurationProperties jobConfiguration;

    @StepScope
    @Bean(AUDIT_EXPORT_READER)
    public JpaPagingItemReader<AuditMessage> auditExportReader(
            @Qualifier(READ_ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory,
            @Value("#{jobParameters['archiveStartDate']}") Date archiveStartDate,
            @Value("#{jobParameters['archiveEndDate']}") Date archiveEndDate) {
        String jpaQuery;
        Map<String, Object> params;
        if (archiveStartDate != null ) {
            params = Map.of("startDate", archiveStartDate, "endDate", archiveEndDate);
            jpaQuery = "SELECT a FROM AuditMessage a WHERE a.eventTime BETWEEN :startDate AND :endDate";
        } else {
            params = Map.of("endDate", archiveEndDate);
            jpaQuery = "SELECT a FROM AuditMessage a WHERE a.eventTime <= :endDate";
        }
        return new JpaPagingItemReaderBuilder<AuditMessage>()
                .name(AUDIT_EXPORT_READER)
                .entityManagerFactory(entityManagerFactory)
                .queryString(jpaQuery)
                .parameterValues(params)
                .pageSize(jobConfiguration.getExport().getChunkSize())
                .saveState(true)
                .build();
    }
}
