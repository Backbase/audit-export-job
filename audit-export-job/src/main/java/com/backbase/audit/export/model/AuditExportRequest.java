package com.backbase.audit.export.model;

import com.backbase.audit.export.service.validator.ValidDateRange;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@ValidDateRange
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuditExportRequest {
    private long requestId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Valid
    private LocalDate archiveStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Valid
    private LocalDate archiveEndDate;
    private int retentionDays;
    private String frequency;
    private int exportDays;
}
