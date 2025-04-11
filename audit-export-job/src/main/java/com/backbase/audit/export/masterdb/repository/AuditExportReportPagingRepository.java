package com.backbase.audit.export.masterdb.repository;

import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditExportReportPagingRepository extends PagingAndSortingRepository<AuditExportReport, UUID> {
    Page<AuditExportReport> findByJobExecutionId(Long jobExecutionId, Pageable pageable);
}