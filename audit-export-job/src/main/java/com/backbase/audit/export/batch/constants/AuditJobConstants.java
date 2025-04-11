package com.backbase.audit.export.batch.constants;

import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuditJobConstants {
    public static final String DELIMITER = ",";
    public static final String CUSTOM_EXECUTOR_SERVICE = "customExecutorService";

    public static final String ENTITY_MANAGER_FACTORY = "entityManagerFactory";
    public static final String TRANSACTION_MANAGER = "transactionManager";
    public static final String AUDIT_EXPORT_JOB = "auditExportJob";
    public static final String AUDIT_EXPORT_STEP = "auditExportStep";
    public static final String AUDIT_EXPORT_READER = "auditExportReader";

    public static final String AUDIT_DATA_SOURCE = "auditDataSource";
    public static final String AUDIT_TRANSACTION_MANAGER = "auditTransactionManager";
    public static final String AUDIT_ENTITY_MANAGER_FACTORY = "auditEntityManagerFactory";
    public static final String READ_DATA_SOURCE = "readDataSource";
    public static final String READ_ENTITY_MANAGER_FACTORY = "readEntityManagerFactory";
    public static final String READ_TRANSACTION_MANAGER = "readTransactionManager";

    public static final String AUDIT_FILE_REMOVAL_READER = "auditFileRemovalReader";
    public static final String AUDIT_FILE_REMOVAL_STEP = "auditFileRemovalStep";
    public static final String AUDIT_FILE_REMOVAL_JOB = "auditFileRemovalJob";

    public static final String AUDIT_MESSAGE_CLEANUP_READER = "auditMessageCleanupReader";
    public static final String AUDIT_MESSAGE_CLEANUP_STEP = "auditMessageCleanupStep";
    public static final String AUDIT_MESSAGE_CLEANUP_JOB = "auditMessageCleanupJob";

    public static final String EXPORT_JOB_ID = "exportJobId";
    public static final String ARCHIVE_START_DATE = "archiveStartDate";
    public static final String ARCHIVE_END_DATE = "archiveEndDate";

    public static final Map<String, Integer> FREQUENCY_DAYS_MAP =
        Map.of("daily", 1, "weekly", 7, "monthly", 30, "yearly", 365, "quarterly", 90, "half-yearly", 180);
}
