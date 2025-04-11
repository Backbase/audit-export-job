package com.backbase.audit.export.service;

import com.backbase.audit.export.model.AuditFileRemovalRequest;
import com.backbase.audit.export.model.JobExecutionResponse;
import com.backbase.audit.export.utils.ReportBuilder;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_FILE_REMOVAL_JOB;
import static com.backbase.audit.export.batch.constants.AuditJobConstants.EXPORT_JOB_ID;

@Slf4j
@Service
public class AuditFileRemovalBatchService {

    private final JobLauncher jobLauncher;
    private final Job auditFileRemovalJob;

    public AuditFileRemovalBatchService(JobLauncher jobLauncher,
                                        @Qualifier(AUDIT_FILE_REMOVAL_JOB) Job auditFileRemovalJob) {
        this.jobLauncher = jobLauncher;
        this.auditFileRemovalJob = auditFileRemovalJob;
    }

    public JobExecutionResponse runAuditFileRemoval(AuditFileRemovalRequest request) {
        try {
            var params = new JobParametersBuilder()
                    .addLong(AUDIT_FILE_REMOVAL_JOB, request.getRequestId())
                    .addLong(EXPORT_JOB_ID, request.getExportJobId())
                    .toJobParameters();
            var job = jobLauncher.run(auditFileRemovalJob, params);
            return ReportBuilder.getBatchJobDetailByExecution(job);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn(e.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            throw new InternalServerErrorException("An error occurred while processing job.", e);
        }
    }
}
