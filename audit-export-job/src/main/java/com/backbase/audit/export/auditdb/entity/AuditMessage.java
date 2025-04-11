package com.backbase.audit.export.auditdb.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="audit_message")
@Data
public class AuditMessage implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "actor_user_id")
    private String actorUserId;

    @Column(name = "event_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date eventTime;

    @Column(name = "status")
    private String status;

    @Column(name = "actor_username")
    private String actorUsername;

    @Column(name = "event_category")
    private String eventCategory;

    @Column(name = "object_type")
    private String objectType;

    @Column(name = "event_action")
    private String eventAction;

    @Column(name = "event_desc")
    private String eventDesc;

    @Column(name = "message_set_id")
    private String messageSetId;

    @Column(name = "legal_entity_id")
    private String legalEntityId;

    @Column(name = "sa_id")
    private String saId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "schema_version")
    private String schemaVersion;

    @Column(name = "error")
    private String error;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "additions")
    private String additions;

    @Column(name = "temp_legacy_id")
    private String tempLegacyId;

    @Column(name = "emulator_user_id")
    private String emulatorUserId;

    @Column(name = "emulator_username")
    private String emulatorUsername;

    @Column(name = "emulator_domain")
    private String emulatorDomain;

}