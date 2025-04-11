package com.backbase.audit.export.masterdb.entity;

import com.backbase.audit.export.model.AuditDocumentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="audit_export_report")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditExportReport implements Serializable {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "job_execution_id")
    private Long jobExecutionId;

    @Column(name = "archive_start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date archiveStartDate;

    @Column(name = "archive_end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date archiveEndDate;

    @Column(name = "repository_id")
    private String repositoryId;

    @Column(name = "min_event_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date minEventTime;

    @Column(name = "max_event_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date maxEventTime;

    @Column(name = "path")
    private String path;

    @Column(name = "object_id")
    private String objectId;

    @Column(name = "event_id_list")
    private String eventIdList;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AuditDocumentStatus status = AuditDocumentStatus.ACTIVE;
}