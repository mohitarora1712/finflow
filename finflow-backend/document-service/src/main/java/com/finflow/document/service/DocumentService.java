package com.finflow.document.service;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.finflow.document.entity.Document;
import com.finflow.document.entity.DocumentStatus;
import com.finflow.document.messaging.DocumentEventPublisher;
import com.finflow.document.repository.DocumentRepository;
import com.finflow.document.storage.FileStorageService;


@Service
public class DocumentService {

 private final DocumentRepository repo;
 private final FileStorageService storage;
 private final DocumentEventPublisher publisher;

 public DocumentService(
   DocumentRepository repo,
   FileStorageService storage,
   DocumentEventPublisher publisher) {

  this.repo = repo;
  this.storage = storage;
  this.publisher = publisher;
 }

 public Document upload(UUID appId, String type, MultipartFile file) {

  String path = storage.store(appId, file);

  Document doc = new Document();
  doc.setApplicationId(appId);
  doc.setDocType(type);
  doc.setFilePath(path);
  doc.setStatus(DocumentStatus.UPLOADED);
  doc.setUploadedAt(LocalDateTime.now());

  repo.save(doc);

  publisher.publishUploaded(appId);

  return doc;
 }

 public Document verify(UUID id) {

  Document doc = repo.findById(id).orElseThrow();

  doc.setStatus(DocumentStatus.VERIFIED);
  doc.setVerifiedAt(LocalDateTime.now());

  repo.save(doc);

  publisher.publishVerified(doc.getApplicationId());

  return doc;
 }

 public Resource download(UUID id) {

  Document doc = repo.findById(id).orElseThrow();

  try {
   return new UrlResource(Paths.get(doc.getFilePath()).toUri());
  } catch (Exception e) {
   throw new RuntimeException("Download failed");
  }
 }
}
