package com.backbase.audit.export.rest;

import com.backbase.audit.export.model.AuditExportRequest;
import com.backbase.audit.export.model.AuditFileRemovalRequest;
import com.backbase.audit.export.model.JobExecutionResponse;
import com.backbase.audit.export.service.AuditExportBatchService;
import com.backbase.audit.export.service.AuditFileRemovalBatchService;
import com.backbase.stream.abb.spec.clientapi.v1.AuditFileRemovalApi;
import com.backbase.stream.abb.spec.clientapi.v1.AuditLogExportApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Profile({"local","dev"})
@RequiredArgsConstructor
@Slf4j
@RestController
public class AuditExportBatchController implements AuditLogExportApi, AuditFileRemovalApi {
    private final AuditExportBatchService auditExportBatchService;
    private final AuditFileRemovalBatchService auditFileRemovalBatchService;

    @Override
    public ResponseEntity<JobExecutionResponse> runAuditExport(AuditExportRequest auditExportRequest, Boolean force) {
        auditExportBatchService.validateAuditExportRequest(auditExportRequest, Optional.ofNullable(force).orElse(false));
        var job = auditExportBatchService.runAuditExport(auditExportRequest);
        return ResponseEntity.ok(job);
    }

    @Override
    public ResponseEntity<JobExecutionResponse> runAuditFileRemoval(@PathVariable Long exportJobId) {
        var job = auditFileRemovalBatchService.runAuditFileRemoval(
                new AuditFileRemovalRequest(1, exportJobId));
        return ResponseEntity.ok(job);
    }
}
