package com.backbase.audit.export.batch.step;

import com.backbase.audit.export.batch.writer.AuditFileRemovalWriter;
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

import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_FILE_REMOVAL_READER;
import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_FILE_REMOVAL_STEP;

@Slf4j
@Configuration
public class AuditFileRemovalStepConfig {

    @Bean(AUDIT_FILE_REMOVAL_STEP)
    public Step auditFileRemovalStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     @Qualifier(AUDIT_FILE_REMOVAL_READER) JpaPagingItemReader<AuditExportReport> auditFileRemovalReader,
                                     AuditFileRemovalWriter writer,
                                     JobConfigurationProperties properties) {
       return new StepBuilder(AUDIT_FILE_REMOVAL_STEP, jobRepository)
                .<AuditExportReport, AuditExportReport>chunk(properties.getCsvRemoval().getChunkSize(), transactionManager)
                .reader(auditFileRemovalReader)
                .writer(writer)
                .faultTolerant()
                .skipLimit(0)
                .skip(FileNotFoundException.class)
                .skip(NullPointerException.class)
               .build();
    }
}
