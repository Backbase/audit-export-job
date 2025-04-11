package com.backbase.audit.export.service;

import com.backbase.audit.export.auditdb.entity.AuditMessage;
import com.backbase.audit.export.auditdb.repository.AuditMessageRepository;
import com.backbase.audit.export.config.JobConfigurationProperties;
import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import com.backbase.audit.export.utils.ReportBuilder;
import com.backbase.audit.export.utils.ZipStringUtils;
import com.backbase.cxp.contentservice.model.v2.DocumentToSave;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CustomAuditExportServiceTest {

    @Mock
    private ContentRepositoryService contentRepositoryService;
    @Mock
    private ContentStreamService contentStreamService;
    @Mock
    private AuditFileReportService auditFileReportService;
    @Mock
    private AuditMessageRepository auditMessageRepository;

    @Mock
    private JobConfigurationProperties jobConfiguration;

    private CustomAuditExportService customAuditExportService;

    @BeforeEach
    public void setup() {
        var customExecutor = Executors.newWorkStealingPool();
        customAuditExportService = new CustomAuditExportService(contentRepositoryService, contentStreamService, auditFileReportService, auditMessageRepository, jobConfiguration, customExecutor);
    }

    @Test
    void processAuditRecordsInBatches() {
        var auditMessage = new AuditMessage();
        auditMessage.setId(1L);
        auditMessage.setEventTime(new Date());
        var auditMessages = List.of(auditMessage);
        var csvContent = ZipStringUtils.compress(ReportBuilder.convertToCsvContent(auditMessages));
        var doc = new DocumentToSave();
        doc.setContent(csvContent);
        doc.setRepositoryId("1");
        doc.setId("1");
        doc.setPath("sample-path");
        var exportJob = new JobConfigurationProperties.ExportJob();
        exportJob.setBatchWriteSize(1);
        exportJob.setIntegrityCheck(true);
        exportJob.setDownloadCheck(true);
        when(jobConfiguration.getExport()).thenReturn(exportJob);

        when(contentRepositoryService.uploadContentInPath(anyString(), anyString())).thenReturn(doc);
        Mockito.lenient().when(contentStreamService.renderContentStreamById(doc.getRepositoryId(), doc.getId(), false)).thenReturn(csvContent);
        doNothing().when(auditFileReportService).saveExportFileReport(anyLong(), any(Date.class), any(Date.class),
                any(DocumentToSave.class), anyString(), any(Date.class), any(Date.class));

        customAuditExportService.processAuditRecordsInBatches(auditMessages, 1L, new Date(), new Date());
        verify(contentRepositoryService, times(1)).uploadContentInPath(anyString(), anyString());
        verify(auditFileReportService, times(1)).saveExportFileReport(anyLong(), any(Date.class), any(Date.class),
                any(DocumentToSave.class), anyString(), any(Date.class), any(Date.class));
        verify(contentStreamService, times(1)).renderContentStreamById(anyString(), anyString(), anyBoolean());
    }

    @Test
    void removeAuditFilesInBatches() {
        var report = new AuditExportReport();
        var reports = List.of(report);
        var removalJob = new JobConfigurationProperties.RemovalJob();
        removalJob.setBatchWriteSize(1);
        when(jobConfiguration.getCsvRemoval()).thenReturn(removalJob);
        doNothing().when(contentRepositoryService).removeDocuments(anyList(), any());
        customAuditExportService.removeAuditFilesInBatches(reports);
        verify(contentRepositoryService, times(1)).removeDocuments(anyList(), any());
    }

    @Test
    void cleanupAuditMessages() {
        doNothing().when(auditMessageRepository).deleteAuditMessagesByIds(anyList());
        var cleanupJob = new JobConfigurationProperties.CleanupJob();
        cleanupJob.setBatchWriteSize(1);
        when(jobConfiguration.getCleanup()).thenReturn(cleanupJob);
        var report = new AuditExportReport();
        List<Long> auditMessageIds = List.of(1L, 2L, 3L, 4L);
        report.setEventIdList("1,2,3,4");
        var result = customAuditExportService.cleanupAuditMessages(report);
        assertThat(result).isEqualTo(auditMessageIds.size());
        verify(auditMessageRepository, times(4)).deleteAuditMessagesByIds(anyList());
    }

}
