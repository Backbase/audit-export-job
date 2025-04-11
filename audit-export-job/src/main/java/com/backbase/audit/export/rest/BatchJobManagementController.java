package com.backbase.audit.export.rest;

import com.backbase.audit.export.model.AuditExportResponse;
import com.backbase.audit.export.model.ItemListResponse;
import com.backbase.audit.export.model.JobExecutionResponse;
import com.backbase.audit.export.service.AuditFileReportService;
import com.backbase.audit.export.utils.ReportBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_EXPORT_JOB;
import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_FILE_REMOVAL_JOB;

@Profile({"local","dev"})
@Slf4j
@RestController
@RequiredArgsConstructor
public class BatchJobManagementController {
    private final JobExplorer jobExplorer;
    private final AuditFileReportService auditFileReportService;

    @GetMapping("/client-api/v2/batch/reports")
    public ResponseEntity<ItemListResponse<AuditExportResponse>> findByJobExecutionId(
            @RequestParam(defaultValue = "0") Long jobId,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        var list = auditFileReportService.getReportByJobId(jobId, pageNo, pageSize);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/client-api/v2/batch/count-audit-events")
    public ResponseEntity<Long> countDbItems(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        var total = auditFileReportService.countTotalDbItems(
                startDate,
                endDate);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/client-api/v2/batch/last")
    public ResponseEntity<List<JobExecutionResponse>> getLastJobExecution() {
        var jobs = Stream.of(AUDIT_EXPORT_JOB, AUDIT_FILE_REMOVAL_JOB)
                .map(jobExplorer::getLastJobInstance)
                .filter(Objects::nonNull)
                .map(jobExplorer::getJobExecutions)
                .flatMap(Collection::stream)
                .map(ReportBuilder::getBatchJobDetailByExecution)
                .toList();
        return ResponseEntity.ok(jobs);

    }

    @GetMapping("/client-api/v2/batch/execution/{executionId}")
    public ResponseEntity<JobExecutionResponse> getBatchJobDetail(@PathVariable Long executionId) {
        var job = jobExplorer.getJobExecution(executionId);
        assert job != null;
        return ResponseEntity.ok(ReportBuilder.getBatchJobDetailByExecution(job));
    }
}
