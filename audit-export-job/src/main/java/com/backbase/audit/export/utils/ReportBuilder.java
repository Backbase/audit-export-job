package com.backbase.audit.export.utils;

import com.backbase.audit.export.auditdb.entity.AuditMessage;
import com.backbase.audit.export.model.JobExecutionResponse;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.backbase.audit.export.batch.constants.AuditJobConstants.*;


@Slf4j
@UtilityClass
public class ReportBuilder {
    private final String FILE_NAME = "audit-export.csv";
    private final String PATH_SEPARATOR = "/";
    private final String DASH = "_";
    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public CSVWriter buildCSVWriter(OutputStreamWriter streamWriter) {
        return new CSVWriter(streamWriter, ICSVWriter.DEFAULT_SEPARATOR, ICSVWriter.DEFAULT_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, System.lineSeparator());
    }

    public String buildOutputFileURL(Date archiveStartDate, Date archiveEndDate) {
        StringBuilder outputFileUrl = new StringBuilder(PATH_SEPARATOR);
        if (archiveStartDate != null) {
            outputFileUrl.append(dateFormat.format(archiveStartDate)).append(DASH);
        }
        return outputFileUrl.append(dateFormat.format(archiveEndDate)).append(DASH).append(UUID.randomUUID()).append(DASH).append(FILE_NAME).toString();
    }

    public List<String[]> buildCsvRecords(List<AuditMessage> auditMessageList, boolean includeHeader) {
        List<String[]> csvRecordsList = new ArrayList<>();
        List<String> headerList = Arrays.stream(AuditMessage.class.getDeclaredFields()).map(Field::getName).toList();
        if (includeHeader) {
            csvRecordsList.add(headerList.toArray(new String[0]));
        }
        auditMessageList.forEach(auditMessage -> {
            var dataMap = convertToMap(auditMessage);
            List<String> dataRecords = headerList.stream().map(dataMap::get).toList();
            csvRecordsList.add(dataRecords.toArray(new String[0]));
        });

        return csvRecordsList;
    }

    public List<String[]> buildCsvRecords(List<AuditMessage> auditMessageList) {
        return buildCsvRecords(auditMessageList, true);
    }

    public String convertToCsvContent(List<AuditMessage> auditMessages) {
        String content;
        try (var stream = new ByteArrayOutputStream();
             var streamWriter = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
             var writer = ReportBuilder.buildCSVWriter(streamWriter)) {
            var csvRecordsList = ReportBuilder.buildCsvRecords(auditMessages);
            writer.writeAll(csvRecordsList);
            writer.flush();
            content = StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Exception occurred while writing audit messages content to csv file",e);
            throw new InternalServerErrorException().withKey("AUDIT_EXPORT_FAILED").withMessage(e.getMessage());
        }
        return content;
    }

    private Map<String, String> convertToMap(AuditMessage obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(obj, new TypeReference<>() {});
    }

    public String calculateDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null) {
            if (endTime == null) {
                endTime = LocalDateTime.now(ZoneId.systemDefault());
            }
            var durationInMillis = startTime.until(endTime, ChronoUnit.MILLIS);
            long millis = durationInMillis % 1000;
            long second = (durationInMillis / 1000) % 60;
            long minute = (durationInMillis / (1000 * 60)) % 60;
            long hour = (durationInMillis / (1000 * 60 * 60)) % 24;
            return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        } else {
            return StringUtils.EMPTY;
        }
    }

    public JobExecutionResponse getBatchJobDetailByExecution(JobExecution job) {
        String jobName = job.getJobInstance().getJobName();
        JobExecutionResponse.JobParams jobParams = null;
        log.debug("JobExecution parameters {}", job.getJobParameters());
        if (AUDIT_EXPORT_JOB.equalsIgnoreCase(jobName)) {
            jobParams = JobExecutionResponse.JobParams.builder()
                    .archiveStartDate(job.getJobParameters().getDate(ARCHIVE_START_DATE))
                    .archiveEndDate(job.getJobParameters().getDate(ARCHIVE_END_DATE))
                    .build();
        } else if (AUDIT_FILE_REMOVAL_JOB.equalsIgnoreCase(jobName)) {
            jobParams = JobExecutionResponse.JobParams.builder()
                    .exportJobId(job.getJobParameters().getLong(EXPORT_JOB_ID))
                    .build();
        }
        var result = JobExecutionResponse.builder()
                .id(job.getId())
                .jobName(jobName)
                .jobParams(jobParams);
        return job.getStepExecutions().stream().findFirst().map(stepEx -> {
            var startTime = stepEx.getStartTime();
            var endTime = stepEx.getEndTime();
            return result
                    .status(stepEx.getStatus())
                    .existStatus(stepEx.getExitStatus())
                    .readCount(stepEx.getReadCount())
                    .writeCount(stepEx.getWriteCount())
                    .startTime(startTime)
                    .endTime(endTime)
                    .duration(calculateDuration(startTime, endTime))
                    .build();
        }).orElse(result.build());
    }
}
