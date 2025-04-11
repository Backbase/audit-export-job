package com.backbase.audit.export.batch.listener;

import com.backbase.audit.export.service.AuditMessageCleanupBatchService;
import com.backbase.audit.export.service.ContentRepositoryService;
import com.backbase.audit.export.utils.ReportBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_EXPORT_JOB;

@RequiredArgsConstructor
@Component
@Slf4j
public class AuditExportJobListener implements JobExecutionListener {
    private final ObjectMapper objectMapper;
    private final ContentRepositoryService contentRepositoryService;
    private final AuditMessageCleanupBatchService auditMessageCleanupBatchService;

    @SneakyThrows
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("{} - Before processing: {}", AUDIT_EXPORT_JOB, objectMapper.writeValueAsString(ReportBuilder.getBatchJobDetailByExecution(jobExecution)));
        contentRepositoryService.createExportRepositoryIfNotExists();
    }

    @SneakyThrows
    @Override
    public void afterJob(JobExecution jobExecution) {
        var batchJobDetail = ReportBuilder.getBatchJobDetailByExecution(jobExecution);
        log.info("{} - After processing: {} ", jobExecution.getJobInstance().getJobName(), objectMapper.writeValueAsString(batchJobDetail));
        if (ExitStatus.COMPLETED.equals(batchJobDetail.getExistStatus())) {
            auditMessageCleanupBatchService.runAuditMessageCleanup(batchJobDetail.getId());
        }
    }
}
