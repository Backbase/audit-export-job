package com.backbase.audit.export.model;

import java.util.List;

public record ItemListResponse<T>(long totalDbItems, long totalUploadItems, List<T> items) {
}
