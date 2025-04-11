package com.backbase.audit.export.auditdb.repository;

import com.backbase.audit.export.auditdb.config.ReadOnlyRepository;
import com.backbase.audit.export.auditdb.entity.AuditMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@ReadOnlyRepository
@Repository
public interface ReadOnlyAuditRepository extends JpaRepository<AuditMessage, Long> {

    long countByEventTimeLessThanEqual(Date eventTime);
    long countByEventTimeGreaterThanEqualAndEventTimeLessThanEqual(Date fromTime, Date toTime);
}