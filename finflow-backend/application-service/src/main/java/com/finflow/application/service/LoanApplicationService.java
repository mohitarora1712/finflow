package com.finflow.application.service;

import com.finflow.application.config.DecisionEventPublisher;
import com.finflow.application.context.IdentityContext;
import com.finflow.application.dto.AdminDecisionRequest;
import com.finflow.application.dto.CreateDraftRequest;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.entity.LoanApplicationStatus;
import com.finflow.application.entity.LoanStatusHistory;
import com.finflow.application.repository.LoanApplicationRepository;
import com.finflow.application.repository.StatusHistoryRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LoanApplicationService {
	
	
	private static final Logger log = LoggerFactory.getLogger(LoanApplicationService.class);
	
    private final LoanApplicationRepository repo;
    private final StatusHistoryRepository historyRepo;
    private final DecisionEventPublisher publisher;

    public LoanApplicationService(
            LoanApplicationRepository repo,
            StatusHistoryRepository historyRepo,
            DecisionEventPublisher publisher
    ) {
        this.repo = repo;
        this.historyRepo = historyRepo;
        this.publisher = publisher;
    }

    // ================= USER DASHBOARD =================

    @Cacheable(value = "dashboard",
            key = "T(com.finflow.application.context.IdentityContext).getEmail()")
    public List<LoanApplication> getUserApps() {
        String email = IdentityContext.getEmail();
        return repo.findByUserEmail(email);
    }

    // ================= CREATE DRAFT =================

    @CacheEvict(value = "dashboard",
            key = "T(com.finflow.application.context.IdentityContext).getEmail()")
    public LoanApplication createDraft(CreateDraftRequest req) {

        String email = IdentityContext.getEmail();

        LoanApplication app = new LoanApplication();

        // 🔐 USER
        app.setUserEmail(email);

        // 💰 CORE (mandatory)
        app.setAmount(req.getAmount());
        app.setTenureMonths(req.getTenureMonths());
        app.setPurpose(req.getPurpose());

        // 👤 PERSONAL (optional)
        app.setFullName(req.getFullName());
        app.setPhone(req.getPhone());

        // 💼 EMPLOYMENT (optional)
        app.setEmploymentType(req.getEmploymentType());
        app.setCompanyName(req.getCompanyName());

        // 💰 FINANCIAL (optional)
        app.setIncome(req.getIncome());

        // ⚙️ SYSTEM FIELDS
        app.setStatus(LoanApplicationStatus.DRAFT);
        app.setCreatedAt(LocalDateTime.now());

        repo.save(app);

        saveHistory(app.getId(), LoanApplicationStatus.DRAFT, "Draft created");

        log.info("Creating draft for user={}", email);

        return app;
    }

    @CacheEvict(value = "dashboard",
            key = "T(com.finflow.application.context.IdentityContext).getEmail()")
    public void deleteDraft(UUID id) {
        LoanApplication app = repo.findById(id).orElseThrow();
        enforceOwnership(app);
        if (app.getStatus() != LoanApplicationStatus.DRAFT) {
            throw new RuntimeException("Only drafts can be deleted");
        }
        repo.delete(app);
        log.info("Deleted draft id={}", id);
    }

    @CacheEvict(value = "dashboard",
            key = "T(com.finflow.application.context.IdentityContext).getEmail()")
    public LoanApplication updateDraft(UUID id, CreateDraftRequest req) {
        LoanApplication app = repo.findById(id).orElseThrow();
        enforceOwnership(app);
        
        if (app.getStatus() != LoanApplicationStatus.DRAFT) {
            throw new RuntimeException("Only drafts can be updated");
        }

        app.setAmount(req.getAmount());
        app.setTenureMonths(req.getTenureMonths());
        app.setPurpose(req.getPurpose());
        app.setFullName(req.getFullName());
        app.setPhone(req.getPhone());
        app.setEmploymentType(req.getEmploymentType());
        app.setCompanyName(req.getCompanyName());
        app.setIncome(req.getIncome());

        repo.save(app);
        log.info("Updated draft id={}", id);
        return app;
    }

    // ================= SUBMIT =================

    @CacheEvict(value = "dashboard",
            key = "T(com.finflow.application.context.IdentityContext).getEmail()")
    public LoanApplication submit(UUID id) {

        LoanApplication app = repo.findById(id).orElseThrow();

        enforceOwnership(app);

        validateTransition(app.getStatus(), LoanApplicationStatus.SUBMITTED);

        app.setStatus(LoanApplicationStatus.SUBMITTED);
        app.setSubmittedAt(LocalDateTime.now());

        repo.save(app);

        saveHistory(id, LoanApplicationStatus.SUBMITTED, "Application submitted");
        log.info("Submitting application id={}", id);
        return app;
    }

    // ================= DOCS UPLOADED EVENT =================

    @CacheEvict(value = "dashboard", allEntries = true)
    public LoanApplication markDocsUploaded(UUID id) {

        LoanApplication app = repo.findById(id).orElseThrow();

        validateTransition(app.getStatus(), LoanApplicationStatus.DOCS_UPLOADED);

        app.setStatus(LoanApplicationStatus.DOCS_UPLOADED);
        repo.save(app);

        saveHistory(id, LoanApplicationStatus.DOCS_UPLOADED, "Documents uploaded");

        return app;
    }

    // ================= ADMIN WORKFLOW =================

    @CacheEvict(value = "dashboard", allEntries = true)
    public LoanApplication markUnderVerification(UUID id) {

        LoanApplication app = repo.findById(id).orElseThrow();

        validateTransition(app.getStatus(), LoanApplicationStatus.UNDER_VERIFICATION);

        app.setStatus(LoanApplicationStatus.UNDER_VERIFICATION);
        repo.save(app);

        saveHistory(id, LoanApplicationStatus.UNDER_VERIFICATION, "Admin started verification");

        return app;
    }

    @CacheEvict(value = "dashboard", allEntries = true)
    public LoanApplication moveToReview(UUID id) {

        LoanApplication app = repo.findById(id).orElseThrow();

        validateTransition(app.getStatus(), LoanApplicationStatus.UNDER_REVIEW);

        app.setStatus(LoanApplicationStatus.UNDER_REVIEW);
        repo.save(app);

        saveHistory(id, LoanApplicationStatus.UNDER_REVIEW, "Verification completed");

        return app;
    }

    @CacheEvict(value = "dashboard", allEntries = true)
    public LoanApplication decision(UUID id, AdminDecisionRequest req) {

        LoanApplication app = repo.findById(id).orElseThrow();

        if (req.getStatus() == null)
            throw new RuntimeException("Decision status required");

        validateTransition(app.getStatus(), req.getStatus());

        app.setStatus(req.getStatus());
        app.setDecisionAt(LocalDateTime.now());
        app.setRemark(req.getRemark());
        repo.save(app);

        saveHistory(id, req.getStatus(), req.getRemark());
        publisher.publishDecision(app, req.getRemark());
        log.info("Admin decision on application id={} status={}", id, req.getStatus());
        return app;
    }

    // ================= ADMIN DASHBOARD =================

    public List<LoanApplication> getAll() {
    	log.info("Fetching all applications (admin)");
        return repo.findAll();
    }

    // ================= OWNERSHIP =================

    private void enforceOwnership(LoanApplication app) {
        String email = IdentityContext.getEmail();

        if (!app.getUserEmail().equals(email)) {
            throw new RuntimeException("Forbidden: Not application owner");
        }
    }

    // ================= STATE MACHINE =================

    private void validateTransition(LoanApplicationStatus current, LoanApplicationStatus target) {

        if (current == LoanApplicationStatus.DRAFT && target == LoanApplicationStatus.SUBMITTED) return;

        if (current == LoanApplicationStatus.SUBMITTED && target == LoanApplicationStatus.DOCS_UPLOADED) return;

        if (current == LoanApplicationStatus.DOCS_UPLOADED && target == LoanApplicationStatus.UNDER_VERIFICATION) return;

        if (current == LoanApplicationStatus.UNDER_VERIFICATION && target == LoanApplicationStatus.UNDER_REVIEW) return;

        if (current == LoanApplicationStatus.UNDER_REVIEW &&
                (target == LoanApplicationStatus.APPROVED || target == LoanApplicationStatus.REJECTED)) return;

        throw new RuntimeException("Invalid state transition");
    }

    // ================= HISTORY =================

    private void saveHistory(UUID id, LoanApplicationStatus status, String remark) {

        LoanStatusHistory h = new LoanStatusHistory();
        h.setApplicationId(id);
        h.setStatus(status);
        h.setRemark(remark);
        h.setTimestamp(LocalDateTime.now());

        historyRepo.save(h);
    }
}