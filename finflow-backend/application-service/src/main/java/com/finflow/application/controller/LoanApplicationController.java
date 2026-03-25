package com.finflow.application.controller;

import com.finflow.application.dto.AdminDecisionRequest;
import com.finflow.application.dto.CreateDraftRequest;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.service.LoanApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/applications")
public class LoanApplicationController {

    private final LoanApplicationService service;

    public LoanApplicationController(LoanApplicationService service) {
        this.service = service;
    }

    @PostMapping("/draft")
    public LoanApplication draft(
            @RequestHeader("X-User-Email") String email,
            @RequestBody CreateDraftRequest req) {
        return service.createDraft(email, req);
    }

    @PostMapping("/{id}/submit")
    public LoanApplication submit(
            @PathVariable UUID id,
            @RequestHeader("X-User-Email") String email) {
        return service.submit(id, email);
    }

    @GetMapping("/my")
    public List<LoanApplication> dashboard(
            @RequestHeader("X-User-Email") String email) {
        return service.getUserApps(email);
    }

    // 🔥 ADMIN ONLY
    @PatchMapping("/{id}/decision")
    public LoanApplication decision(
            @PathVariable UUID id,
            @RequestBody AdminDecisionRequest req) {
        return service.decision(id, req);
    }
}