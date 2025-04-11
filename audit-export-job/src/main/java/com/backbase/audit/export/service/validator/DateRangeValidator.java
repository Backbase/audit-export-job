package com.backbase.audit.export.service.validator;

import com.backbase.audit.export.model.AuditExportRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.ZoneId;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, AuditExportRequest> {
        @Override
        public boolean isValid(AuditExportRequest value, ConstraintValidatorContext context) {
            if (value.getArchiveStartDate() != null && value.getArchiveEndDate() != null) {
                return !value.getArchiveStartDate().isAfter(value.getArchiveEndDate()) &&
                        !value.getArchiveEndDate().isAfter(LocalDate.now(ZoneId.systemDefault()));
            } else if (value.getArchiveEndDate() != null) {
                return !value.getArchiveEndDate().isAfter(LocalDate.now(ZoneId.systemDefault()));
            } else {
                return true;
            }
        }
    }
