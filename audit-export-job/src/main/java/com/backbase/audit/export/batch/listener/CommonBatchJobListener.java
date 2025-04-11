package com.backbase.audit.export.batch.listener;

import com.backbase.audit.export.utils.ReportBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommonBatchJobListener implements JobExecutionListener {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("{} - Before processing: {}", jobExecution.getJobInstance().getJobName(), objectMapper.writeValueAsString(ReportBuilder.getBatchJobDetailByExecution(jobExecution)));
    }

    @SneakyThrows
    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("{} - After processing: {} ", jobExecution.getJobInstance().getJobName(), objectMapper.writeValueAsString(ReportBuilder.getBatchJobDetailByExecution(jobExecution)));
    }
}
