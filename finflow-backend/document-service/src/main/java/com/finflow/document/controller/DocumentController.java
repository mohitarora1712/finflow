package com.finflow.document.controller;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.finflow.document.entity.Document;
import com.finflow.document.service.DocumentService;

@RestController
@RequestMapping("/documents")
public class DocumentController {

 private final DocumentService service;

 public DocumentController(DocumentService service) {
  this.service = service;
 }

 @PostMapping("/upload")
 public Document upload(
  @RequestParam UUID applicationId,
  @RequestParam String type,
  @RequestParam MultipartFile file) {

  return service.upload(applicationId, type, file);
 }

 @PatchMapping("/{id}/verify")
 public Document verify(@PathVariable UUID id) {
  return service.verify(id);
 }

 @GetMapping("/{id}/download")
 public ResponseEntity<Resource> download(@PathVariable UUID id) {

  Resource res = service.download(id);

  return ResponseEntity.ok()
   .header(HttpHeaders.CONTENT_DISPOSITION,
      "attachment; filename=\"file\"")
   .body(res);
 }
}
