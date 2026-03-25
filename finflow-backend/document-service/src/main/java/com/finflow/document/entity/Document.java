package com.finflow.document.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "documents")
public class Document implements Serializable {

 private static final long serialVersionUID = 1L;

 @Id
 @GeneratedValue  
 private UUID id;

 private UUID applicationId;

 private String docType;
 private String filePath;

 @Enumerated(EnumType.STRING)
 private DocumentStatus status;

 private LocalDateTime uploadedAt;
 private LocalDateTime verifiedAt;

 public Document() {}

 public UUID getId() {
	return id;
 }

 public void setId(UUID id) {
	this.id = id;
 }

 public UUID getApplicationId() {
	return applicationId;
 }

 public void setApplicationId(UUID applicationId) {
	this.applicationId = applicationId;
 }

 public String getDocType() {
	return docType;
 }

 public void setDocType(String docType) {
	this.docType = docType;
 }

 public String getFilePath() {
	return filePath;
 }

 public void setFilePath(String filePath) {
	this.filePath = filePath;
 }

 public DocumentStatus getStatus() {
	return status;
 }

 public void setStatus(DocumentStatus status) {
	this.status = status;
 }

 public LocalDateTime getUploadedAt() {
	return uploadedAt;
 }

 public void setUploadedAt(LocalDateTime uploadedAt) {
	this.uploadedAt = uploadedAt;
 }

 public LocalDateTime getVerifiedAt() {
	return verifiedAt;
 }

 public void setVerifiedAt(LocalDateTime verifiedAt) {
	this.verifiedAt = verifiedAt;
 }

 public static long getSerialversionuid() {
	return serialVersionUID;
 }

 // getters setters
 
}
