package com.backbase.audit.export.mapper;

import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import com.backbase.audit.export.model.AuditExportResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuditExportReportMapper {
    AuditExportResponse mapAuditExportReport(AuditExportReport entity);
}
