package com.backbase.audit.export.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuditExportResponse {
    private String id;
    private Long jobExecutionId;
    private Date archiveStartDate;
    private Date archiveEndDate;
    private String repositoryId;
    private String path;
    private Date minEventTime;
    private Date maxEventTime;
    private String objectId;
}