package com.backbase.audit.export.service;

import com.backbase.audit.export.auditdb.repository.ReadOnlyAuditRepository;
import com.backbase.audit.export.mapper.AuditExportReportMapper;
import com.backbase.audit.export.mapper.AuditExportReportMapperImpl;
import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import com.backbase.audit.export.masterdb.repository.AuditExportReportPagingRepository;
import com.backbase.audit.export.masterdb.repository.AuditExportReportRepository;
import com.backbase.cxp.contentservice.model.v2.DocumentToSave;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AuditExportReportMapperImpl.class)
class AuditFileReportServiceTest {
    @Mock
    private AuditExportReportRepository auditExportReportRepository;
    @Mock
    private AuditExportReportPagingRepository auditExportReportPagingRepository;
    @Mock
    private ReadOnlyAuditRepository auditRepository;
    @Autowired
    AuditExportReportMapper mapper;

    private AuditFileReportService auditFileReportService;

    @BeforeEach
    public void setup() {
        auditFileReportService = new AuditFileReportService(auditExportReportRepository, auditExportReportPagingRepository,
                auditRepository, mapper);
    }

    @Test
    void getReportByJobId() {
        Integer pageNo = 1;
        Integer pageSize = 10;
        var report = new AuditExportReport();
        report.setEventIdList("1,2");
        report.setArchiveStartDate(mock(Date.class));
        report.setArchiveEndDate(mock(Date.class));
        var content = List.of(report);
        Page<AuditExportReport> page = new PageImpl<>(content);

        when(auditExportReportPagingRepository.findByJobExecutionId(anyLong(), any(Pageable.class))).thenReturn(page);
        when(auditRepository.countByEventTimeGreaterThanEqualAndEventTimeLessThanEqual(any(Date.class), any(Date.class)))
                .thenReturn(10L);
        lenient().when(auditRepository.countByEventTimeLessThanEqual(any(Date.class)))
                .thenReturn(10L);

        var results = auditFileReportService.getReportByJobId(1L, pageNo, pageSize);
        log.info("{}" ,results);
        assertThat(results.totalDbItems()).isEqualTo(10);
        assertThat(results.totalUploadItems()).isEqualTo(2);
        assertThat(results.items()).hasSize(1);
    }

    @Test
    void countTotalDbItems() {
        when(auditRepository.countByEventTimeGreaterThanEqualAndEventTimeLessThanEqual(any(Date.class), any(Date.class)))
                .thenReturn(10L);
        lenient().when(auditRepository.countByEventTimeLessThanEqual(any(Date.class)))
                .thenReturn(20L);
        var result = auditFileReportService.countTotalDbItems(LocalDate.now(), LocalDate.now());
        assertThat(result).isEqualTo(10);
        result = auditFileReportService.countTotalDbItems(null, LocalDate.now());
        assertThat(result).isEqualTo(20);
    }

    @Test
    void saveExportFileReport() {
        when(auditExportReportRepository.save(any(AuditExportReport.class))).thenReturn(mock(AuditExportReport.class));
        auditFileReportService.saveExportFileReport(1L, mock(Date.class), mock(Date.class),
                mock(DocumentToSave.class), "1,2", mock(Date.class), mock(Date.class));
        verify(auditExportReportRepository, times(1)).save(any(AuditExportReport.class));
    }

    @Test
    void updateReportStatus() {
        List<AuditExportReport> reports = List.of(mock(AuditExportReport.class));
        when(auditExportReportRepository.saveAll(anyList())).thenReturn(reports);
        auditFileReportService.updateReportStatus(reports);
        verify(auditExportReportRepository, times(1)).saveAll(reports);
    }
}
