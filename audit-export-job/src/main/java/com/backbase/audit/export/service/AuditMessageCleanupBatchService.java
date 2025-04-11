package com.backbase.audit.export.service;

import com.backbase.audit.export.config.JobConfigurationProperties;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_MESSAGE_CLEANUP_JOB;
import static com.backbase.audit.export.batch.constants.AuditJobConstants.EXPORT_JOB_ID;

@Slf4j
@Service
public class AuditMessageCleanupBatchService {
    private final JobLauncher jobLauncher;
    private final Job auditMessageCleanupJob;
    private final JobConfigurationProperties jobConfiguration;


    public AuditMessageCleanupBatchService(JobLauncher jobLauncher,
                                           @Qualifier(AUDIT_MESSAGE_CLEANUP_JOB) Job auditMessageCleanupJob,
                                           JobConfigurationProperties jobConfiguration) {
        this.jobLauncher = jobLauncher;
        this.auditMessageCleanupJob = auditMessageCleanupJob;
        this.jobConfiguration = jobConfiguration;
    }

    public void runAuditMessageCleanup(Long exportJobId) {
        if (jobConfiguration.getCleanup().isEnabled() && jobConfiguration.getExport().isDaily()) {
            try {
                var params = new JobParametersBuilder()
                        .addLong(AUDIT_MESSAGE_CLEANUP_JOB, System.currentTimeMillis())
                        .addLong(EXPORT_JOB_ID, exportJobId)
                        .toJobParameters();
                jobLauncher.run(auditMessageCleanupJob, params);
            } catch (Exception e) {
                throw new InternalServerErrorException("An error occurred while processing job", e);
            }
        }
    }
}
