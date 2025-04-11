package com.backbase.audit.export.masterdb.repository;

import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import com.backbase.audit.export.model.AuditDocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditExportReportRepository extends JpaRepository<AuditExportReport, UUID> {

    @Query("SELECT count(*) FROM AuditExportReport a WHERE status = :status AND (:startDate BETWEEN a.archiveStartDate AND a.archiveEndDate) OR (:endDate BETWEEN a.archiveStartDate AND a.archiveEndDate)")
    long countOverlapDateRange(AuditDocumentStatus status, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    List<AuditExportReport> findByJobExecutionIdAndStatus(Long jobExecutionId, AuditDocumentStatus status);
}