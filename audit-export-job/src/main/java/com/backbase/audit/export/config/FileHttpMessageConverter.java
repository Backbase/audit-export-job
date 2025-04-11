package com.backbase.audit.export.config;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileHttpMessageConverter extends AbstractHttpMessageConverter<File> {

    public FileHttpMessageConverter() {
        super(StandardCharsets.UTF_8, MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return File.class.equals(clazz);
    }

    @Override
    protected File readInternal(Class<? extends File> clazz, HttpInputMessage inputMessage) throws IOException {
        File tempFile = File.createTempFile("downloaded", ".tmp");
        try (InputStream inputStream = inputMessage.getBody();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    @Override
    protected void writeInternal(File file, HttpOutputMessage outputMessage) throws IOException {
        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = outputMessage.getBody()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}