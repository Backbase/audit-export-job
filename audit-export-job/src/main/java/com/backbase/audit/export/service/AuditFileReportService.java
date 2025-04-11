package com.backbase.audit.export.service;

import com.backbase.audit.export.auditdb.repository.ReadOnlyAuditRepository;
import com.backbase.audit.export.mapper.AuditExportReportMapper;
import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import com.backbase.audit.export.masterdb.repository.AuditExportReportPagingRepository;
import com.backbase.audit.export.masterdb.repository.AuditExportReportRepository;
import com.backbase.audit.export.model.AuditDocumentStatus;
import com.backbase.audit.export.model.AuditExportResponse;
import com.backbase.audit.export.model.ItemListResponse;
import com.backbase.audit.export.utils.DateUtils;
import com.backbase.cxp.contentservice.model.v2.DocumentToSave;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.DELIMITER;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditFileReportService {
    private final AuditExportReportRepository auditExportReportRepository;
    private final AuditExportReportPagingRepository auditExportReportPagingRepository;
    private final ReadOnlyAuditRepository auditRepository;
    private final AuditExportReportMapper mapper;

    private List<AuditExportReport> findByJobExecutionId(Long jobId, Integer pageNo, Integer pageSize) {
        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC,"archiveEndDate"));
        Page<AuditExportReport> pagedResult = auditExportReportPagingRepository.findByJobExecutionId(jobId, paging);
        return pagedResult.hasContent() ? pagedResult.getContent() : Collections.emptyList();
    }

    public ItemListResponse<AuditExportResponse> getReportByJobId(Long jobId, Integer pageNo, Integer pageSize) {
        List<AuditExportReport> exportReports = findByJobExecutionId(jobId, pageNo, pageSize);
        AuditExportReport firstEntity = exportReports.stream().findFirst().orElseThrow();
        long totalDbItems = this.countTotalDbItems(firstEntity.getArchiveStartDate(), firstEntity.getArchiveEndDate());
        var totalUploadItems = (long) exportReports.stream()
                .map(AuditExportReport::getEventIdList)
                .map(AuditFileReportService::convert)
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
                .size();
        var list = exportReports.stream()
                .map(mapper::mapAuditExportReport)
                .toList();
        return new ItemListResponse<>(totalDbItems, totalUploadItems, list);
    }

    private static Set<String> convert(String eventIds) {
        return Arrays.stream(eventIds.split(DELIMITER))
                .collect(Collectors.toSet());
    }

    public long countTotalDbItems(LocalDate startDate, LocalDate endDate) {
        Date startTime = Optional.ofNullable(startDate).map(DateUtils::convertToDate).orElse(null);
        Date endTime = Optional.of(endDate).map(DateUtils::convertToDate).map(DateUtils::getEndOfDayTime).orElseThrow();
        return countTotalDbItems(startTime, endTime);
    }

    private long countTotalDbItems(Date startTime, Date endTime) {
        long totalDbItems;
        if (startTime != null) {
            totalDbItems = auditRepository.countByEventTimeGreaterThanEqualAndEventTimeLessThanEqual(startTime, endTime);
        } else {
            totalDbItems = auditRepository.countByEventTimeLessThanEqual(endTime);
        }
        return totalDbItems;
    }

    public void saveExportFileReport(Long jobExecutionId, Date archiveStartDate, Date archiveEndDate,
                                      DocumentToSave documentToSave, String eventIdList,
                                      Date minEventTime, Date maxEventTime) {
        var entity = AuditExportReport.builder()
                .jobExecutionId(jobExecutionId)
                .archiveStartDate(archiveStartDate)
                .archiveEndDate(archiveEndDate)
                .repositoryId(documentToSave.getRepositoryId())
                .minEventTime(minEventTime)
                .maxEventTime(maxEventTime)
                .objectId(documentToSave.getId())
                .path(documentToSave.getPath())
                .eventIdList(eventIdList)
                .status(AuditDocumentStatus.ACTIVE)
                .build();
        auditExportReportRepository.save(entity);
    }

    public void updateReportStatus(List<AuditExportReport> reports) {
        reports.forEach(r -> r.setStatus(AuditDocumentStatus.REMOVED));
        auditExportReportRepository.saveAll(reports);
    }
}
