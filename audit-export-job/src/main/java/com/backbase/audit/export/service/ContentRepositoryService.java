package com.backbase.audit.export.service;


import com.backbase.audit.export.config.ContentServiceConfigurationProperties;
import com.backbase.audit.export.masterdb.entity.AuditExportReport;
import com.backbase.audit.export.utils.ZipStringUtils;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.cxp.contentservice.api.v2.ContentManagementServiceToServiceApi;
import com.backbase.cxp.contentservice.api.v2.RepositoryManagementServiceToServiceApi;
import com.backbase.cxp.contentservice.model.v2.Document;
import com.backbase.cxp.contentservice.model.v2.DocumentToSave;
import com.backbase.cxp.contentservice.model.v2.RemoveRequest;
import com.backbase.cxp.contentservice.model.v2.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentRepositoryService {


    private final RepositoryManagementServiceToServiceApi repositoryManagementApi;
    private final ContentManagementServiceToServiceApi contentManagementApi;
    private final ContentServiceConfigurationProperties contentProperties;

    private boolean isRepositoryExists() {
        try {
            Repository repositoryResponse = repositoryManagementApi.getRepositoryServiceToService(contentProperties.getAuditRepoId()).block();

            if (null != repositoryResponse && StringUtils.isNotEmpty(repositoryResponse.getRepositoryId())) {
                log.debug("Successfully fetched the details of repository id: {}", contentProperties.getAuditRepoId());
                return true;
            } else {
                log.error("Something went wrong while checking the existence of repository and resultant response is: {}", repositoryResponse);
            }
        } catch (Exception e) {
            log.error("Exception {} occurred while fetching the repository details with id: {}", e, contentProperties.getAuditRepoId());
        }
        return false;
    }

    public DocumentToSave uploadContentInPath(String outputFilePath, String content) {
        DocumentToSave documentToSave = createDocumentToSave(outputFilePath, content);
        try {
            List<Document> responseEntity = contentManagementApi.saveContentServiceToService(false, List.of(documentToSave)).collectList().block();
            if (!CollectionUtils.isEmpty(responseEntity)) {
                var document = responseEntity.get(0);
                documentToSave.setId(document.getId());
                log.debug("Document is successfully uploaded at path: {} in repository {}, object id {}", document.getPath(), document.getRepositoryId(), document.getId());
            } else {
                log.error("The upload api has given empty response at path: {} in repository: {}", documentToSave.getPath(),
                    documentToSave.getRepositoryId());
                throw new InternalServerErrorException("EMPTY_UPLOADED");
            }
        } catch (Exception e) {
            log.error("Exception {} occurred while uploading the content at path {} in repository {}", e, documentToSave.getPath(),
                documentToSave.getRepositoryId());
            throw e;
        }
        return documentToSave;
    }

    private DocumentToSave createDocumentToSave(String outputFilePath, String content) {
        DocumentToSave documentToSave = new DocumentToSave();
        documentToSave.setRepositoryId(contentProperties.getAuditRepoId());
        documentToSave.setContent(ZipStringUtils.compress(content));
        documentToSave.setPath(outputFilePath);
        documentToSave.setType("cmis:document");
        documentToSave.setMimeType("text/plain");
        return documentToSave;
    }

    public void createExportRepositoryIfNotExists() {
        if(!isRepositoryExists()) {
            log.info("About to create repository: {}", contentProperties.getAuditRepoId());
            var repository = buildRepository();
            repositoryManagementApi.createRepositoriesServiceToService(List.of(repository)).block();
            log.info("Successfully created repository: {}", contentProperties.getAuditRepoId());
        }
    }

    private Repository buildRepository() {
        Repository repository = new Repository();
        repository.setRepositoryId(contentProperties.getAuditRepoId());
        repository.setDescription("The repository to storage all the audit export csv files");
        repository.setName(contentProperties.getAuditRepoName());
        repository.setImplementation(contentProperties.getStorageImpl());
        repository.setVersioningEnabled(true);
        repository.setAntivirusScanTrigger(null);
        repository.setIsPrivate(contentProperties.isPrivateRepo());
        return repository;
    }

    public void removeDocuments(List<AuditExportReport> reports, Consumer<Void> consumer) {
        var documentIds = reports.stream()
                .map(AuditExportReport::getObjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        if (CollectionUtils.isEmpty(documentIds)) {
            throw new BadRequestException("No document ids found");
        }
        contentManagementApi.removeContentServiceToService(new RemoveRequest()
                .repositoryId(contentProperties.getAuditRepoId())
                .ids(documentIds))
                .doOnSuccess(consumer)
                .doOnError(err -> log.error(err.getMessage()))
                .block();
    }
}
