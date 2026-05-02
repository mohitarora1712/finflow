package com.finflow.admin.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.finflow.admin.dto.AdminDecisionRequest;
import com.finflow.admin.dto.DocumentResponse;
import com.finflow.admin.dto.LoanApplicationResponse;
import com.finflow.admin.service.AdminWorkflowService;

@RestController
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminWorkflowService service;

    public AdminController(AdminWorkflowService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/applications")
    public List<LoanApplicationResponse> getApps() {
        log.info("ADMIN CONTROLLER: Request received to fetch all applications");
        return service.getAllApps();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/documents/{id}/verify")
    public DocumentResponse verify(@PathVariable UUID id) {
        log.info("ADMIN CONTROLLER: Request to verify document id={}", id);
        return service.verifyDoc(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/applications/{id}/decision")
    public LoanApplicationResponse decide(
            @PathVariable UUID id,
            @RequestBody AdminDecisionRequest req
    ) {
        log.info("ADMIN CONTROLLER: Decision request for application id={}", id);
        return service.decide(id, req);
    }
}