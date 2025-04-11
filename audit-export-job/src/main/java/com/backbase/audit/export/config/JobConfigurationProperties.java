package com.backbase.audit.export.config;


import com.backbase.audit.export.model.AuditExportRequest;
import com.backbase.audit.export.model.AuditFileRemovalRequest;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Data
@Configuration
@NoArgsConstructor
@ConfigurationProperties(prefix = "backbase.audit.batch")
@Validated
public class JobConfigurationProperties {

    private ExportJob export;
    private CleanupJob cleanup;
    private RemovalJob csvRemoval;

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ExportJob extends Base {
        @Valid
        private AuditExportRequest params;
        private boolean integrityCheck;
        private boolean downloadCheck;
        private boolean daily;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class CleanupJob extends Base {}

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class RemovalJob extends Base {
        @Valid
        private AuditFileRemovalRequest params;
    }

    @Data
    public static class Base {
        private int chunkSize;
        private int batchWriteSize;
        private boolean enabled;
    }
}
