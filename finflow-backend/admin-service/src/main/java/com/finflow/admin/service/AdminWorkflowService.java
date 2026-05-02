package com.finflow.admin.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.finflow.admin.client.ApplicationClient;
import com.finflow.admin.client.DocumentClient;
import com.finflow.admin.dto.AdminDecisionRequest;
import com.finflow.admin.dto.DocumentResponse;
import com.finflow.admin.dto.LoanApplicationResponse;

@Service
public class AdminWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(AdminWorkflowService.class);

    private final ApplicationClient appClient;
    private final DocumentClient docClient;

    public AdminWorkflowService(ApplicationClient appClient, DocumentClient docClient) {
        this.appClient = appClient;
        this.docClient = docClient;
    }

    // 🔥 CIRCUIT BREAKER (ONLY HERE)
    @CircuitBreaker(name = "applicationService", fallbackMethod = "fallbackApps")
    public List<LoanApplicationResponse> getAllApps() {
        log.info("ADMIN SERVICE: Fetching all applications from Application Service");
        return appClient.getAll();
    }

    // ⚠️ IMPORTANT: return type MUST match original method
    public List<LoanApplicationResponse> fallbackApps(Throwable t) {
        log.error("Application Service DOWN, fallback triggered: {}", t.getMessage());

        // returning empty list (safe + type correct)
        return List.of();
    }

    // NEW CHANGE
    @CircuitBreaker(name = "documentService", fallbackMethod = "fallbackDoc")
    public DocumentResponse verifyDoc(UUID docId) {
        log.info("ADMIN SERVICE: Verifying document with id={}", docId);
        return docClient.verify(docId);
    }

    // NEW CHANGE
    public DocumentResponse fallbackDoc(UUID docId, Throwable t) {
        log.error("Document Service DOWN, fallback triggered for docId={}: {}", docId, t.getMessage());
        DocumentResponse response = new DocumentResponse();
        response.setId(docId);
        response.setStatus("SERVICE_UNAVAILABLE");
        return response;
    }

    // NEW CHANGE
    @CircuitBreaker(name = "applicationService", fallbackMethod = "fallbackDecide")
    public LoanApplicationResponse decide(UUID appId, AdminDecisionRequest req) {
        log.info("ADMIN SERVICE: Taking decision on application id={}", appId);
        return appClient.decide(appId, req);
    }

    // NEW CHANGE
    public LoanApplicationResponse fallbackDecide(UUID appId, AdminDecisionRequest req, Throwable t) {
        log.error("Application Service DOWN, fallback triggered for decision on appId={}: {}", appId, t.getMessage());
        LoanApplicationResponse response = new LoanApplicationResponse();
        response.setId(appId);
        response.setStatus("PENDING_RETRY");
        return response;
    }
}