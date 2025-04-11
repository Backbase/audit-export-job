package com.backbase.audit.export.batch.writer;

import com.backbase.audit.export.auditdb.entity.AuditMessage;
import com.backbase.audit.export.service.CustomAuditExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.*;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class AuditExportWriter implements ItemWriter<AuditMessage>, StepExecutionListener {
    private final CustomAuditExportService customAuditExportService;
    @Value("#{jobParameters['archiveStartDate']}")
    private Date archiveStartDate;
    @Value("#{jobParameters['archiveEndDate']}")
    private Date archiveEndDate;
    private JobExecution jobExecution;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.jobExecution = stepExecution.getJobExecution();
    }

    @Override
    public void write(Chunk<? extends AuditMessage> chunk) {
        var  auditMessages = chunk.getItems().stream().map(i -> (AuditMessage) i).toList();
        log.debug("{} {} - Writing {} audit messages to content-services", AUDIT_EXPORT_JOB,
                jobExecution.getId(), auditMessages.size());
        customAuditExportService.processAuditRecordsInBatches(auditMessages,
                jobExecution.getId(),
                archiveStartDate,
                archiveEndDate);
    }
}
