package com.backbase.audit.export.batch.writer;

import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import com.backbase.audit.export.service.CustomAuditExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.AUDIT_FILE_REMOVAL_JOB;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class AuditFileRemovalWriter implements ItemWriter<AuditExportReport>, StepExecutionListener {
    private final CustomAuditExportService customAuditExportService;
    private JobExecution jobExecution;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.jobExecution = stepExecution.getJobExecution();
    }

    @Override
    public void write(Chunk<? extends AuditExportReport> chunk) {
        log.debug("{} {} - Removing audit files from content-services", AUDIT_FILE_REMOVAL_JOB, jobExecution.getId());
        customAuditExportService.removeAuditFilesInBatches(chunk.getItems().stream().map(i -> (AuditExportReport) i).toList());
    }
}
