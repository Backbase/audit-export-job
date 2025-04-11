package com.backbase.audit.export.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;

import java.time.LocalDateTime;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JobExecutionResponse {
    private Long id;
    private String jobName;
    private BatchStatus status;
    private ExitStatus existStatus;
    private Long readCount;
    private Long writeCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String duration;
    private JobParams jobParams;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    @Data
    public static class JobParams {
        private Date archiveStartDate;
        private Date archiveEndDate;
        private Long exportJobId;
    }
}
