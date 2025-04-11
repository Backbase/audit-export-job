package com.backbase.audit.export.config;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @copyright (C) 2022, Backbase
 * @Version 1.0
 * @Since 01. Nov 2022 3:36 pm
 */
@Setter
@ToString
@Getter
@Configuration
@NoArgsConstructor
@ConfigurationProperties(prefix = "backbase.stream.cxp")
@Validated
public class ContentServiceConfigurationProperties {

    @Valid
    @NotNull
    private String contentServiceUrl;

    @Valid
    @NotNull
    private String storageImpl;

    @Valid
    @NotNull
    private String auditRepoId;

    @Valid
    @NotNull
    private String auditRepoName;

    @Valid
    @NotNull
    private boolean privateRepo;

}
