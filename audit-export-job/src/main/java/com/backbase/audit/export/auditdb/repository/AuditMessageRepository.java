package com.backbase.audit.export.auditdb.repository;

import com.backbase.audit.export.auditdb.entity.AuditMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditMessageRepository extends JpaRepository<AuditMessage, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM AuditMessage e WHERE e.id IN :ids")
    void deleteAuditMessagesByIds(List<Long> ids);
}