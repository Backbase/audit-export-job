package com.backbase.audit.export.service;

import com.backbase.audit.export.config.JobConfigurationProperties;
import com.backbase.audit.export.masterdb.repository.AuditExportReportRepository;
import com.backbase.audit.export.model.AuditDocumentStatus;
import com.backbase.audit.export.model.AuditExportRequest;
import com.backbase.audit.export.model.JobExecutionResponse;
import com.backbase.audit.export.utils.DateUtils;
import com.backbase.audit.export.utils.ReportBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.*;

@Slf4j
@Service
public class AuditExportBatchService {
    private final JobLauncher jobLauncher;
    private final Job auditExportJob;
    private final AuditExportReportRepository repository;
    private final JobConfigurationProperties jobConfiguration;


    public AuditExportBatchService(JobLauncher jobLauncher,
                                   @Qualifier(AUDIT_EXPORT_JOB) Job auditExportJob, AuditExportReportRepository repository, JobConfigurationProperties jobConfiguration) {
        this.jobLauncher = jobLauncher;
        this.auditExportJob = auditExportJob;
        this.repository = repository;
        this.jobConfiguration = jobConfiguration;
    }

    public void validateAuditExportRequest(AuditExportRequest auditExportRequest, boolean forceRun) {
        if (!forceRun) {
            long overlapDateRangeCount = repository.countOverlapDateRange(AuditDocumentStatus.ACTIVE,
                    DateUtils.convertToDate(auditExportRequest.getArchiveStartDate()),
                    DateUtils.convertToDate(auditExportRequest.getArchiveEndDate()));
            if (overlapDateRangeCount > 0) {
                log.debug("overlap date range count {}", overlapDateRangeCount);
                throw new BadRequestException("Overlap date range");
            }
        }
    }

    public @NotNull JobExecutionResponse runAuditExport(@Valid AuditExportRequest auditExportRequest) {
        try {
            var archiveStartDate = Optional.ofNullable(auditExportRequest.getArchiveStartDate()).map(DateUtils::convertToDate);
            String requestId = String.valueOf(auditExportRequest.getRequestId());
            if (jobConfiguration.getExport().isDaily()) {
               requestId = DateUtils.currentDateText().concat(requestId);
            }
            var parametersBuilder = new JobParametersBuilder().addString(AUDIT_EXPORT_JOB, requestId);
            archiveStartDate.ifPresent(date -> parametersBuilder.addDate(ARCHIVE_START_DATE, date));
            var archiveEndDate = DateUtils.convertToDate(auditExportRequest.getArchiveEndDate());
            var params = parametersBuilder
                    .addDate(ARCHIVE_END_DATE, DateUtils.getEndOfDayTime(archiveEndDate))
                    .toJobParameters();
            var job = jobLauncher.run(auditExportJob, params);
            return ReportBuilder.getBatchJobDetailByExecution(job);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn(e.getLocalizedMessage());
            return null;
        } catch (Exception e) {
            throw new InternalServerErrorException("An error occurred while processing job", e);
        }
    }
}
