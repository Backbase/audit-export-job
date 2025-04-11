package com.backbase.audit.export.batch;

import com.backbase.audit.export.batch.listener.AuditExportJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_EXPORT_JOB;
import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_EXPORT_STEP;

@Configuration
public class AuditExportByDateBatchJobConfig {
    @Bean(AUDIT_EXPORT_JOB)
    public Job auditExportJob(JobRepository jobRepository,
                              AuditExportJobListener listener,
                              @Qualifier(AUDIT_EXPORT_STEP) Step auditExportStep) {
        return new JobBuilder(AUDIT_EXPORT_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(auditExportStep)
                .end()
                .build();
    }


}
