package com.finflow.document.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finflow.document.entity.Document;

public interface DocumentRepository
extends JpaRepository<Document, UUID> {

List<Document> findByApplicationId(UUID appId);

}
