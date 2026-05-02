package com.finflow.application.controller;

import com.finflow.application.dto.AdminDecisionRequest;
import com.finflow.application.dto.CreateDraftRequest;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.service.LoanApplicationService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class LoanApplicationController {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationController.class);

    private final LoanApplicationService service;

    public LoanApplicationController(LoanApplicationService service) {
        this.service = service;
    }

    // ================= USER =================

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/draft")
    public LoanApplication draft(@Valid @RequestBody CreateDraftRequest req) {
        log.info("APPLICATION CONTROLLER: Creating draft");
        return service.createDraft(req);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/draft/{id}")
    public LoanApplication updateDraft(@PathVariable UUID id, @Valid @RequestBody CreateDraftRequest req) {
        log.info("APPLICATION CONTROLLER: Updating draft id={}", id);
        return service.updateDraft(id, req);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/draft/{id}")
    public void deleteDraft(@PathVariable UUID id) {
        log.info("APPLICATION CONTROLLER: Deleting draft id={}", id);
        service.deleteDraft(id);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}/submit")
    public LoanApplication submit(@PathVariable UUID id) {
        log.info("APPLICATION CONTROLLER: Submitting application id={}", id);
        return service.submit(id);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<LoanApplication> dashboard() {
        log.info("APPLICATION CONTROLLER: Fetching user dashboard");
        return service.getUserApps();
    }

    // ================= ADMIN =================

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/decision")
    public LoanApplication decision(
            @PathVariable UUID id,
            @RequestBody AdminDecisionRequest req) {

        log.info("APPLICATION CONTROLLER: Decision on application id={}", id);
        return service.decision(id, req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public List<LoanApplication> getAllForAdmin() {
        log.info("APPLICATION CONTROLLER: Fetching all applications for admin");
        return service.getAll();
    }
}