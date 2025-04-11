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
public class AuditFileRemovalBatchJobConfig {
    @Bean(AUDIT_FILE_REMOVAL_JOB)
    public Job auditFileRemovalJob(JobRepository jobRepository,
                                   CommonBatchJobListener listener,
                                   @Qualifier(AUDIT_FILE_REMOVAL_STEP) Step auditFileRemovalStep) {
        return new JobBuilder(AUDIT_FILE_REMOVAL_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(auditFileRemovalStep)
                .end()
                .build();
    }
}
