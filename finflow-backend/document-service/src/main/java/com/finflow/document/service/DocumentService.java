package com.finflow.document.service;

import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.finflow.document.context.IdentityContext;
import com.finflow.document.entity.Document;
import com.finflow.document.entity.DocumentStatus;
import com.finflow.document.messaging.DocumentEventPublisher;
import com.finflow.document.repository.DocumentRepository;
import com.finflow.document.storage.FileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DocumentService {
	
	private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

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

 // ================= UPLOAD =================

 public Document upload(UUID appId, String type, MultipartFile file) {

  String email = IdentityContext.getEmail();

  // 🔥 ownership enforcement must be cross-service via application later
  // currently trusting gateway + user flow

  String path = storage.store(appId, file);

  Document doc = new Document();
  doc.setApplicationId(appId);
  doc.setDocType(type);
  doc.setFilePath(path);
  doc.setOriginalFileName(file.getOriginalFilename());
  doc.setStatus(DocumentStatus.UPLOADED);
  doc.setUploadedAt(LocalDateTime.now());

  repo.save(doc);

  publisher.publishUploaded(appId);
  log.info("Uploading document for applicationId={}", appId);
  return doc;
 }

 // ================= VERIFY =================

 public Document verify(UUID id) {

  Document doc = repo.findById(id).orElseThrow();

  doc.setStatus(DocumentStatus.VERIFIED);
  doc.setVerifiedAt(LocalDateTime.now());

  repo.save(doc);

  publisher.publishVerified(doc.getApplicationId());
  log.info("Verifying document id={}", id);
  return doc;
 }

 // ================= DOWNLOAD =================

 public Resource download(UUID id) {

  Document doc = repo.findById(id).orElseThrow();

  String role = IdentityContext.getRole();

  if ("USER".equals(role)) {
   enforceOwnership(doc);
  }

  try {
	  log.info("Downloading document id={}", id);
   return new UrlResource(Paths.get(doc.getFilePath()).toUri());
  } catch (Exception e) {
   throw new RuntimeException("Download failed");
  }
 }

 public Document getById(UUID id) {
  return repo.findById(id).orElseThrow();
 }

 // ================= OWNERSHIP =================

 private void enforceOwnership(Document doc) {

  String email = IdentityContext.getEmail();

  // Ownership enforcement will later be enhanced via Application Feign
  // For now minimal secure version

  if (email == null) {
   throw new RuntimeException("Unauthorized");
  }
 }

 public java.util.List<Document> getDocumentsByApplicationId(UUID appId) {
  log.info("Fetching documents for applicationId={}", appId);
  // Optional ownership check could go here if needed, relying on Gateway/Role for now
  return repo.findByApplicationId(appId);
 }
}