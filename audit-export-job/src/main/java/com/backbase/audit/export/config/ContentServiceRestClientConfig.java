package com.backbase.audit.export.config;

import com.backbase.buildingblocks.communication.client.ApiClientConfig;
import com.backbase.buildingblocks.communication.http.HttpCommunicationConfiguration;
import com.backbase.cxp.contentservice.restTemplate.api.ApiClient;
import com.backbase.cxp.contentservice.restTemplate.api.v2.ContentStreamServiceToServiceApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@ConfigurationProperties(prefix = "backbase.stream.cxp")
public class ContentServiceRestClientConfig extends ApiClientConfig {
    private static final String SERVICE_ID = "contentservices";

    public ContentServiceRestClientConfig() {
        super(SERVICE_ID);
    }

    @Bean
    public ContentStreamServiceToServiceApi contentStreamApiClient() {
        return new ContentStreamServiceToServiceApi(createApiClient());
    }

    private ApiClient createApiClient() {
        var restTemplate = getRestTemplate();
        restTemplate.getMessageConverters().add(new FileHttpMessageConverter());
        return new ApiClient(restTemplate)
                .setBasePath(createBasePath())
                .addDefaultHeader(HttpCommunicationConfiguration.INTERCEPTORS_ENABLED_HEADER, Boolean.TRUE.toString());
    }
}
