package com.backbase.audit.export.scheduler;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_EXPORT_JOB;
import static com.backbase.audit.export.batch.constants.AuditJobConstants.FREQUENCY_DAYS_MAP;

import com.backbase.audit.export.config.JobConfigurationProperties;
import com.backbase.audit.export.model.AuditExportRequest;
import com.backbase.audit.export.service.AuditExportBatchService;
import com.backbase.audit.export.service.AuditFileRemovalBatchService;
import com.backbase.audit.export.service.AuditFileReportService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
@EnableAsync
public class BatchJobScheduler {
    private final AuditExportBatchService auditExportBatchService;
    private final AuditFileReportService auditFileReportService;
    private final AuditFileRemovalBatchService auditFileRemovalBatchService;
    private final JobConfigurationProperties jobConfiguration;
    private final ObjectMapper objectMapper;

    @Async
    @Scheduled(cron = "${backbase.audit.batch.export.cronExpression}")
    public void triggerExportTask() throws JsonProcessingException {
        if (jobConfiguration.getExport().isEnabled() && jobConfiguration.getExport().getParams() != null) {
            var exportRequest = auditExportRequest();
            if (exportRequest.getArchiveEndDate() == null) {
                int days = getDaysToExport(exportRequest);
                if (days > 0) {
                    exportRequest.setArchiveStartDate(LocalDate.now().minusDays(days));
                    exportRequest.setArchiveEndDate(LocalDate.now().minusDays(1));
                } else if (exportRequest.getRetentionDays() > 0) {
                    exportRequest.setArchiveEndDate(LocalDate.now().minusDays(exportRequest.getRetentionDays()));
                } else {
                    log.warn("Invalid parameters {}. Stop batch job {}", exportRequest, AUDIT_EXPORT_JOB);
                    return;
                }
            }

            var jobDetail = auditExportBatchService.runAuditExport(exportRequest);
            if (jobDetail != null) {
                var countDbItems = auditFileReportService.countTotalDbItems(exportRequest.getArchiveStartDate(),
                    exportRequest.getArchiveEndDate());
                log.info("{} {} - Total audit messages found in Audit DB: {}. Job details: {}. ", AUDIT_EXPORT_JOB,
                    jobDetail.getId(), countDbItems, objectMapper.writeValueAsString(jobDetail));

            }
        }
    }

    @Async
    @Scheduled(cron = "${backbase.audit.batch.csvRemoval.cronExpression}")
    public void triggerRemovalTask() {
        if (jobConfiguration.getCsvRemoval().isEnabled() && jobConfiguration.getCsvRemoval().getParams() != null) {
            auditFileRemovalBatchService.runAuditFileRemoval(jobConfiguration.getCsvRemoval().getParams());
        }
    }


    private int getDaysToExport(AuditExportRequest exportRequest) {
        if (Objects.nonNull(exportRequest.getFrequency())) {
            return Optional.ofNullable(FREQUENCY_DAYS_MAP.get(exportRequest.getFrequency().toLowerCase())).orElse(0);
        }
        return exportRequest.getExportDays();
    }

    private AuditExportRequest auditExportRequest() {
        return new AuditExportRequest(
            jobConfiguration.getExport().getParams().getRequestId(),
            jobConfiguration.getExport().getParams().getArchiveStartDate(),
            jobConfiguration.getExport().getParams().getArchiveEndDate(),
            jobConfiguration.getExport().getParams().getRetentionDays(),
            jobConfiguration.getExport().getParams().getFrequency(),
            jobConfiguration.getExport().getParams().getExportDays()
        );
    }
}
