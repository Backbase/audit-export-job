package com.backbase.audit.export.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Configuration
@Slf4j
@EnableBatchProcessing
@EnableTransactionManagement
public class BatchConfig extends DefaultBatchConfiguration  {
    @Primary
    @Override
    @Bean
    public JobLauncher jobLauncher() throws BatchConfigurationException {
        var taskExecutorJobLauncher = new TaskExecutorJobLauncher();
        taskExecutorJobLauncher.setJobRepository(this.jobRepository());
        taskExecutorJobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        try {
            taskExecutorJobLauncher.afterPropertiesSet();
            return taskExecutorJobLauncher;
        } catch (Exception e) {
            log.error("Can't load SimpleJobLauncher with SimpleAsyncTaskExecutor: {} fallback on default", e.getLocalizedMessage());
            throw new BatchConfigurationException("Unable to configure the default job launcher", e);
        }
    }

    @Bean("customExecutorService")
    public ExecutorService customExecutorService(){
        return Executors.newWorkStealingPool();
    }
}