package com.backbase.audit.export.service;

import com.backbase.audit.export.utils.ZipStringUtils;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.cxp.contentservice.restTemplate.api.v2.ContentStreamServiceToServiceApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Service
public class ContentStreamService {
    private final ContentStreamServiceToServiceApi contentStreamServiceToServiceApi;

    public String renderContentStreamById(String repositoryId, String objectId, Boolean decompress)  {
        File contentFile = contentStreamServiceToServiceApi.renderContentStreamByIdServiceToService(repositoryId, objectId);
        assert contentFile != null;
        try {
            var content = FileUtils.readFileToString(contentFile, StandardCharsets.UTF_8);
            return decompress != null && decompress ? ZipStringUtils.decompress(content) : content;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new InternalServerErrorException(e);
        }
    }
}
