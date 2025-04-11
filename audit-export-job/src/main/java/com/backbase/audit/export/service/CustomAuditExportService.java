package com.backbase.audit.export.service;

import com.backbase.audit.export.auditdb.entity.AuditMessage;
import com.backbase.audit.export.auditdb.repository.AuditMessageRepository;
import com.backbase.audit.export.config.JobConfigurationProperties;
import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import com.backbase.audit.export.utils.BatchProcessingHelper;
import com.backbase.audit.export.utils.ReportBuilder;
import com.backbase.audit.export.utils.ZipStringUtils;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.cxp.contentservice.model.v2.DocumentToSave;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.*;

@Service
@Slf4j
public class CustomAuditExportService {

    private final ContentRepositoryService contentRepositoryService;
    private final ContentStreamService contentStreamService;
    private final AuditFileReportService auditFileReportService;
    private final AuditMessageRepository auditMessageRepository;
    private final JobConfigurationProperties jobConfiguration;
    private final ExecutorService customExecutor;

    public CustomAuditExportService(ContentRepositoryService contentRepositoryService,
                                    ContentStreamService contentStreamService,
                                    AuditFileReportService auditFileReportService,
                                    AuditMessageRepository auditMessageRepository,
                                    JobConfigurationProperties jobConfigurationProperties,
                                    @Qualifier(CUSTOM_EXECUTOR_SERVICE) ExecutorService customExecutor) {
        this.contentRepositoryService = contentRepositoryService;
        this.contentStreamService = contentStreamService;
        this.auditFileReportService = auditFileReportService;
        this.auditMessageRepository = auditMessageRepository;
        this.jobConfiguration = jobConfigurationProperties;
        this.customExecutor = customExecutor;
    }

    public void processAuditRecordsInBatches(final List<AuditMessage> auditMessages, final Long jobExecutionId,
                                            final Date archiveStartDate, final Date archiveEndDate) {
        var watch = new StopWatch();
        watch.start("Audit Messages Export");
        Function<List<AuditMessage>, CompletableFuture<Void>> futureFunction = batch -> CompletableFuture.runAsync(() ->
                        this.processAuditRecords(jobExecutionId, batch, archiveStartDate, archiveEndDate), customExecutor);
        BatchProcessingHelper.processBatches(auditMessages, futureFunction, jobConfiguration.getExport().getBatchWriteSize());
        watch.stop();
        log.info("{} {} - Complete batch export {} records in {} milliseconds", AUDIT_EXPORT_JOB, jobExecutionId, auditMessages.size(), watch.getTotalTimeMillis());
    }

    private void processAuditRecords(Long jobExecutionId, List<AuditMessage> auditMessages, Date archiveStartDate, Date archiveEndDate) {
        var eventIdList = auditMessages.stream().map(AuditMessage::getId).map(String::valueOf).collect(Collectors.joining(DELIMITER));
        var minEventTime = auditMessages.stream().map(AuditMessage::getEventTime).min(Date::compareTo).orElseThrow();
        var maxEventTime = auditMessages.stream().map(AuditMessage::getEventTime).max(Date::compareTo).orElseThrow();
        var auditFilePath = ReportBuilder.buildOutputFileURL(minEventTime, maxEventTime);
        var watch = new StopWatch();
        watch.start("Upload Audit File");
        var csvContent = ReportBuilder.convertToCsvContent(auditMessages);
        var document = contentRepositoryService.uploadContentInPath(auditFilePath, csvContent);
        watch.stop();
        log.info("{} {} - Successfully uploaded {} records to {} Time taken: {} milliseconds" , AUDIT_EXPORT_JOB,
                jobExecutionId, auditMessages.size(), document.getPath(), watch.getTotalTimeMillis());
        auditFileReportService.saveExportFileReport(jobExecutionId, archiveStartDate, archiveEndDate, document, eventIdList, minEventTime, maxEventTime);
        this.validateDataIntegrity(jobExecutionId, csvContent, document);
    }

    public void removeAuditFilesInBatches(final List<AuditExportReport> reports) {
        var watch = new StopWatch();
        watch.start("Files Removal");
        Function<List<AuditExportReport>, CompletableFuture<Void>> futureFunction =
                batch -> CompletableFuture.runAsync(() -> this.removeAuditFiles(batch));
        BatchProcessingHelper.processBatches(reports, futureFunction, jobConfiguration.getCsvRemoval().getBatchWriteSize());
        watch.stop();
        log.info("{} - Deletion of {} audit files took {} milliseconds", AUDIT_FILE_REMOVAL_JOB, reports.size(), watch.getTotalTimeMillis());
    }

    private void removeAuditFiles(List<AuditExportReport> reports) {
        contentRepositoryService.removeDocuments(reports, rs -> auditFileReportService.updateReportStatus(reports));
    }

    private void validateDataIntegrity(Long jobExecutionId, String csvContent, DocumentToSave document) {
        if (jobConfiguration.getExport().isIntegrityCheck()) {
            var watch = new StopWatch();
            watch.start("Data Integrity Check");
            String uploadedCsvContent = getUploadedCsvContent(document);
            if (StringUtils.isBlank(csvContent) ||
                    StringUtils.isBlank(uploadedCsvContent) ||
                    !Arrays.equals(ZipStringUtils.compress(csvContent).toCharArray(),uploadedCsvContent.toCharArray())) {
                throw new InternalServerErrorException().withKey("FILE_CONTENT_MISMATCH").withMessage(document.getPath());
            }
            watch.stop();
            log.info("{} {} - Complete data integrity check for file {}. Time taken: {} milliseconds",
                    AUDIT_EXPORT_JOB, jobExecutionId, document.getPath(), watch.getTotalTimeMillis());
        }
    }

    private String getUploadedCsvContent(DocumentToSave document) {
        String csvContent;
        if (jobConfiguration.getExport().isDownloadCheck()) {
            log.debug("Render content stream by id {}", document.getId());
            csvContent = contentStreamService.renderContentStreamById(document.getRepositoryId(), document.getId(), false);
        } else {
            assert document.getContent() != null;
            csvContent = document.getContent();
        }
        return csvContent;
    }

    public int cleanupAuditMessages(AuditExportReport report) {
        var eventIdlist = Arrays.stream(report.getEventIdList().split(DELIMITER))
                .map(Long::valueOf)
                .toList();
        var count = new AtomicInteger(0);
        Function<List<Long>, CompletableFuture<Void>> futureFunction = batch -> CompletableFuture.runAsync(() -> {
            auditMessageRepository.deleteAuditMessagesByIds(batch);
            count.getAndAdd(batch.size());
        });
        BatchProcessingHelper.processBatches(eventIdlist, futureFunction, jobConfiguration.getCleanup().getBatchWriteSize());
        return count.get();
    }
}
