package com.backbase.audit.export.batch.step;

import com.backbase.audit.export.batch.writer.AuditMessageCleanupWriter;
import com.backbase.audit.export.config.JobConfigurationProperties;
import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.FileNotFoundException;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.*;

@Slf4j
@Configuration
public class AuditMessageCleanupStepConfig {

    @Bean(AUDIT_MESSAGE_CLEANUP_STEP)
    public Step auditMessageCleanupStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     @Qualifier(AUDIT_MESSAGE_CLEANUP_READER) JpaPagingItemReader<AuditExportReport> auditMessageCleanupReader,
                                     AuditMessageCleanupWriter writer,
                                     JobConfigurationProperties properties) {
       return new StepBuilder(AUDIT_MESSAGE_CLEANUP_STEP, jobRepository)
                .<AuditExportReport, AuditExportReport>chunk(properties.getCleanup().getChunkSize(), transactionManager)
                .reader(auditMessageCleanupReader)
                .writer(writer)
                .faultTolerant()
                .skipLimit(0)
                .skip(FileNotFoundException.class)
                .skip(NullPointerException.class)
               .build();
    }
}
