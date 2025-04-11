package com.backbase.audit.export.batch.step;

import com.backbase.audit.export.auditdb.entity.AuditMessage;
import com.backbase.audit.export.batch.writer.AuditExportWriter;
import com.backbase.audit.export.config.JobConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.FileNotFoundException;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_EXPORT_STEP;

@Slf4j
@Configuration
public class AuditExportStepConfig {

    @Bean(AUDIT_EXPORT_STEP)
    public Step auditExportStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager,
                                JpaPagingItemReader<AuditMessage> auditExportReader,
                                AuditExportWriter writer,
                                JobConfigurationProperties properties) {
        return new StepBuilder(AUDIT_EXPORT_STEP, jobRepository)
                .<AuditMessage, AuditMessage>chunk(properties.getExport().getChunkSize(), transactionManager)
                .reader(auditExportReader)
                .writer(writer)
                .faultTolerant()
                .skipLimit(0)
                .skip(FileNotFoundException.class)
                .skip(NullPointerException.class)
                .build();
    }
}
