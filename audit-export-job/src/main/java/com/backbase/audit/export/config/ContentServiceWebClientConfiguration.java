package com.backbase.audit.export.config;

import com.backbase.buildingblocks.webclient.WebClientConstants;
import com.backbase.cxp.contentservice.api.ApiClient;
import com.backbase.cxp.contentservice.api.v2.ContentManagementServiceToServiceApi;
import com.backbase.cxp.contentservice.api.v2.ContentUploadServiceToServiceApi;
import com.backbase.cxp.contentservice.api.v2.RepositoryManagementServiceToServiceApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DateFormat;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 01. Nov 2022 3:35 pm
 */
@Configuration
public class ContentServiceWebClientConfiguration {

    @Bean
    public ApiClient contentServiceApiClient(ContentServiceConfigurationProperties properties,
                                             @Qualifier(WebClientConstants.INTER_SERVICE_WEB_CLIENT_NAME) WebClient dbsWebClient,
                                             ObjectMapper objectMapper,
                                             DateFormat dateFormat) {
        ApiClient apiClient = new ApiClient(dbsWebClient, objectMapper, dateFormat);
        apiClient.setBasePath(properties.getContentServiceUrl());
        return apiClient;
    }

    @Bean
    public ContentUploadServiceToServiceApi contentUploadApi(ApiClient apiClient) {
        return new ContentUploadServiceToServiceApi(apiClient);
    }

    @Bean
    public RepositoryManagementServiceToServiceApi repositoryManagementApi(ApiClient apiClient) {
        return new RepositoryManagementServiceToServiceApi(apiClient);
    }

    @Bean
    public ContentManagementServiceToServiceApi contentManagementApi(ApiClient apiClient) {
        return new ContentManagementServiceToServiceApi(apiClient);
    }
}
