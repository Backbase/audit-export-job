# Audit-export-job

## Confluence page
https://backbase.atlassian.net/wiki/spaces/~63f2e1cb89de3d475af3837e/pages/5197725879/EWB+-+Audit+Export+Batch+Job

## Problem statement
Due to high audit data volumes in PROD, the audit-services is slowing down which also causes impact to the Message Queue (Azure Service Bus, AWS SQS, Kafka, ActiveMQ..) (EWB Retail : more than 800Gi audit data)
### Requirements

**One time tasks**

To enhance audit-service performance, we need to achieve legacy audit data (before a specific date) in Audit database by uploading into Azure Blob
Manually cleanup legacy audit data (before a specific date) from Audit database

**Scheduled tasks**

Schedule a daily task to export and upload legacy audit data  onto Blob  (older than X days)
Upon successful export task, run a task to cleanup legacy audit data (older than X days)

## Overview design
![image](https://github.com/user-attachments/assets/200af864-8d67-4798-b455-dffa8731c18c)


### Sequence diagram 
![image](https://github.com/user-attachments/assets/44c58160-515d-40d6-8df9-4aba0d50dae8)


- Trigger audit job batch export based on a cron scheduler (configurable)
- Check and create audit repository <audit-csv-repository> if not exists
- Make sure <audit-csv-repository> has PRIVATE access, only authenticated and authorized users can access this data
- Read user audit messages from Audit Database (Read Only Replica) by date range (archiveStartDate-archiveEndDate)
- Process and **compress** audit data to save upload storage and improve upload speed
- Upload to **Azure Blob Storage** via **content-services**.
- Store one audit file per user with the following output file format: <br>
 File Path: "\<archiveStartDate\>_\<archiveEndDate\>_\<uuid\>_audit-export.csv".
- Upon successful upload, access to file content via an API (protected by access control): <br>
  **Parameters:**
    - _decompress_ (false/true) return file content in compressed text or decompress it to csv format
    - _repositoryId_: storage repository id
    - _contentstream-id_: uploaded document id

    > GET {{gateway}}/api/audit-export-job/client-api/v2/contentstream-id/{{repositoryId}}/{{contentstream-id}}?decompress=true 
  
    **Authorization**

      {
        "functionId": "1013",
        "functionCode": "audit",
        "resource": "Audit",
        "name": "Audit",
        "privileges": [
            {
                "privilege": "view",
                "limits": []
            }
        ]
      }
    

- Check data integrity of the uploaded file by comparing its content with database data (optional, enabled by default)
- Upon successful job run, if the job is daily job, cleanup audit data from main audit db replica (optional, disabled by default)

### Deployment configuration

<details>
<summary>Audit export job</summary>

````shell
  audit-export-job:
    chart: generic-integration
    repoURL: "{{ .sharedACR }}"
    version: "{{ .genericIntegrationVersion }}"
    additionalClasses:
      - common-configs
    tags:
      - custom
    values:
      replicaCount: 1
      fullnameOverride: audit-export-job
      image:
        registry: project
        repository: audit-export-job
        tag: *auditExportVersion
      resources:
        requests:
          cpu: 3000m
          memory: 3Gi
        limits:
          cpu: 7000m
          memory: 8Gi
      mqbroker:
        enabled: false
      database:
        enabled: true
      env:
        spring.autoconfigure.exclude: "org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration"
        server.port: "8080"
        SPRING_LIQUIBASE_ENABLED: "true"
        spring.profiles.active: "json-logging"
        backbase.stream.cxp.contentServiceUrl: "http://contentservices-export:8080"
        backbase.stream.cxp.serviceId: "contentservices-export"
        spring.datasource.hikari.maximum-pool-size: "100"
        spring.datasource.hikari.minimum-idle: "20"
        spring.datasource.hikari.connection-timeout: "45000"
        spring.datasource.hikari.idle-timeout: "600000"
        spring.datasource.hikari.max-lifetime: "1800000"
        spring.datasource.hikari.leak-detection-threshold: "120000"
        spring.jpa.properties.hibernate.default_batch_fetch_size: "1000"
        backbase.audit.batch.export.enabled: "true"
        backbase.audit.batch.export.cronExpression: "0 10 14 16 * ?"
        backbase.audit.batch.export.chunkSize: "500000"
        backbase.audit.batch.export.batchWriteSize: "25000"
        backbase.audit.batch.export.daily: "false"
        backbase.audit.batch.export.params.requestId: "1"
        #backbase.audit.batch.export.params.retentionDays: "30"
        backbase.audit.batch.export.params.archiveStartDate: "2025-01-01"
        backbase.audit.batch.export.params.archiveEndDate: "2025-02-01"
        backbase.audit.batch.cleanup.enabled: "false"
        backbase.audit.batch.cleanup.chunkSize: "100"
        backbase.audit.batch.cleanup.batchWriteSize: "100"
        backbase.audit.batch.csvRemoval.enabled: "false"
        backbase.audit.batch.csvRemoval.params.requestId: "1"
        backbase.audit.batch.csvRemoval.params.exportJobId: "28"
        AUDIT_DB_HOST:
          value:
            configMapKeyRef:
              key: mssql-endpoint
              name: database
              optional: false
        AUDIT_DB_PORT:
          value:
            configMapKeyRef:
              key: mssql-port
              name: database
              optional: false
        AUDIT_DB_SERVERNAME:
          value:
            configMapKeyRef:
              key: mssql-server-name
              name: database
              optional: false
        AUDIT_DB_SID: "audit"
        AUDIT_DB_PASSWORD:
          value:
            secretKeyRef:
              key: db_password
              name: db-audit
        AUDIT_DB_USERNAME:
          value:
            secretKeyRef:
              key: db_username
              name: db-audit
        spring.datasource.read-replica.username: "$(AUDIT_DB_USERNAME)"
        spring.datasource.read-replica.password: "$(AUDIT_DB_PASSWORD)"
        spring.datasource.read-replica.driverClassName: "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        spring.datasource.read-replica.url: "jdbc:sqlserver://$(AUDIT_DB_HOST):$(AUDIT_DB_PORT);database=$(AUDIT_DB_SID);user=$(AUDIT_DB_USERNAME)@$(AUDIT_DB_SERVERNAME);password=$(AUDIT_DB_PASSWORD);encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;applicationIntent=readonly;"
        spring.datasource.auditdb.username: "$(AUDIT_DB_USERNAME)"
        spring.datasource.auditdb.password: "$(AUDIT_DB_PASSWORD)"
        spring.datasource.auditdb.driverClassName: "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        spring.datasource.auditdb.url: "jdbc:sqlserver://$(AUDIT_DB_HOST):$(AUDIT_DB_PORT);database=$(AUDIT_DB_SID);user=$(AUDIT_DB_USERNAME)@$(AUDIT_DB_SERVERNAME);password=$(AUDIT_DB_PASSWORD);encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

````
</details>

<details>
<summary>Content service (cloned)</summary>

````shell
  contentservices-export:
    chart: generic-integration
    repoURL: "{{ .sharedACR }}"
    version: "{{ .genericIntegrationVersion }}"
    additionalClasses:
      - common-configs
      - high-service-resource
    tags:
      - custom
    values:
      fullnameOverride: contentservices-export
      replicaCount: 5
      mqbroker:
        enabled: true
      database:
        enabled: true
      image:
        tag: *backbaseBomVersion
        repository: contentservices
        registry: shared
      env:
        LOGGING_LEVEL_COM_BACKBASE: "DEBUG"
        LOGGING_LEVEL_COM: "DEBUG"
        "spring.cloud.loadbalancer.ribbon.enabled": "false"
        "spring.autoconfigure.exclude": org.springframework.cloud.netflix.eureka.loadbalancer.LoadBalancerEurekaAutoConfiguration
        "SPRING_LIQUIBASE_ENABLED": "true"
        "spring.profiles.active": "json-logging"
        "contentservices.storage.connectors.azureblob.service": "azure-blobstore-connector"
        "contentservices.storage.defaultImplementation": "azureblob"
        "contentservices.storage.signedUrl.enabled": "true"
        "CONTENTSERVICES_USE_AZURE_BLOB_STORAGE_CONNECTOR": "true"
        "spring.cloud.azure.eventhubs.kafka.enabled": "false"
        "contentservices.whitelist.enabled": "true"
        "contentservices.whitelist.allowedContentTypes": "image/jpeg,image/pjpeg,image/gif,image/png,image/svg+xml,application/pdf,application/json,application/zip,application/rtf,text/plain,application/octet-stream,text/html,text/csv,text/x-handlebars-template,message/rfc822,application/x-x509-key"
        "contentservices.antivirus.service": "clamav-antivirus-connector"
        "contentservices.antivirus.check-on-save.enabled": "false"
        "contentservices.antivirus.check-on-query.enabled": "false"
        "spring.servlet.multipart.max-file-size": "15MB"
        "SIG_SECRET_KEY":
          value:
            secretKeyRef:
              name: jwt
              key: jwt-internal-secretkey
              optional: false
````


