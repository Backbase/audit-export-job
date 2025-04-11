package com.backbase.audit.export.batch.reader;

import com.backbase.audit.export.config.JobConfigurationProperties;
import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import com.backbase.audit.export.model.AuditDocumentStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuditFileReportReaderConfig {
    private final JobConfigurationProperties jobConfiguration;

    @StepScope
    @Bean(AUDIT_FILE_REMOVAL_READER)
    public JpaPagingItemReader<AuditExportReport> auditFileRemovalReader(
            EntityManagerFactory entityManagerFactory,
            @Value("#{jobParameters['exportJobId']}") Long exportJobId) {
        return getAuditExportReportJpaPagingItemReader(entityManagerFactory, exportJobId, jobConfiguration.getCsvRemoval().getChunkSize());
    }

    @StepScope
    @Bean(AUDIT_MESSAGE_CLEANUP_READER)
    public JpaPagingItemReader<AuditExportReport> auditMessageCleanupReader(
            EntityManagerFactory entityManagerFactory,
            @Value("#{jobParameters['exportJobId']}") Long exportJobId) {
        return getAuditExportReportJpaPagingItemReader(entityManagerFactory, exportJobId, jobConfiguration.getCleanup().getChunkSize());
    }

    private JpaPagingItemReader<AuditExportReport> getAuditExportReportJpaPagingItemReader(
            EntityManagerFactory entityManagerFactory, Long exportJobId, int pageSize
    ) {
        String jpaQuery = "SELECT a FROM AuditExportReport a WHERE a.jobExecutionId = :exportJobId AND a.status = :status";
        Map<String, Object> params = Map.of("exportJobId", exportJobId, "status", AuditDocumentStatus.ACTIVE);
        return new JpaPagingItemReaderBuilder<AuditExportReport>()
                .name(AUDIT_FILE_REMOVAL_READER)
                .entityManagerFactory(entityManagerFactory)
                .queryString(jpaQuery)
                .parameterValues(params)
                .pageSize(pageSize)
                .saveState(true)
                .build();
    }
}
