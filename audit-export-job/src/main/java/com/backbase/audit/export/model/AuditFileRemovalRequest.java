package com.backbase.audit.export.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuditFileRemovalRequest {
    private long requestId;
    private long exportJobId;
}
