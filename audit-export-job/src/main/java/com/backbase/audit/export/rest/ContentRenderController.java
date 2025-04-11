package com.backbase.audit.export.rest;

import com.backbase.audit.export.service.ContentStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class ContentRenderController {
    private final ContentStreamService contentStreamService;

    @GetMapping("/client-api/v2/contentstream-id/{repositoryId}/{objectId}")
    @PreAuthorize("checkPermission('Audit', 'Audit', {'view'})")
    public ResponseEntity<String> renderContentStream(
        @PathVariable String repositoryId,
        @PathVariable String objectId,
        @RequestParam(required = false) Boolean decompress) {
        return ResponseEntity.ok(contentStreamService.renderContentStreamById(repositoryId, objectId, decompress));
    }

}

