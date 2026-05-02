package com.finflow.document.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.finflow.document.entity.Document;
import com.finflow.document.service.DocumentService;

import org.springframework.http.MediaType;


@RestController
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    // ================= USER =================

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Document upload(
            @RequestParam UUID applicationId,
            @RequestParam String type,
            @RequestParam MultipartFile file) {

        log.info("DOCUMENT CONTROLLER: Upload request for applicationId={}", applicationId);
        return service.upload(applicationId, type, file);
    }

    // ================= ADMIN =================

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/verify")
    public Document verify(@PathVariable UUID id) {
        log.info("DOCUMENT CONTROLLER: Verifying document id={}", id);
        return service.verify(id);
    }

    // ================= BOTH =================

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {

        log.info("DOCUMENT CONTROLLER: Download request id={}", id);

        Document doc = service.getById(id);
        Resource res = service.download(id);

        String contentType = "application/octet-stream";
        try {
            contentType = java.nio.file.Files.probeContentType(java.nio.file.Paths.get(doc.getFilePath()));
        } catch (Exception e) {
            log.warn("Could not determine content type for document id={}", id);
        }

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + (doc.getOriginalFileName() != null ? doc.getOriginalFileName() : "document") + "\"")
                .body(res);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/application/{applicationId}")
    public java.util.List<Document> getDocumentsByApplicationId(@PathVariable UUID applicationId) {
        log.info("DOCUMENT CONTROLLER: Fetching documents for applicationId={}", applicationId);
        return service.getDocumentsByApplicationId(applicationId);
    }
}