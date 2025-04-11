package com.backbase.audit.export.batch;

import com.backbase.audit.export.batch.listener.CommonBatchJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.*;

@Configuration
public class AuditMessageCleanupBatchJobConfig {
    @Bean(AUDIT_MESSAGE_CLEANUP_JOB)
    public Job auditMessageCleanupJob(JobRepository jobRepository,
                                      CommonBatchJobListener listener,
                                   @Qualifier(AUDIT_MESSAGE_CLEANUP_STEP) Step auditMessageCleanupStep) {
        return new JobBuilder(AUDIT_MESSAGE_CLEANUP_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(auditMessageCleanupStep)
                .end()
                .build();
    }
}
