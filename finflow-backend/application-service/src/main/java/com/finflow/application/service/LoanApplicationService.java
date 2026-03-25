package com.finflow.application.service;

import com.finflow.application.config.DecisionEventPublisher;
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

@Service
public class LoanApplicationService {

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

    @Cacheable(value = "dashboard", key = "#email")
    public List<LoanApplication> getUserApps(String email) {
        return repo.findByUserEmail(email);
    }

    @CacheEvict(value = "dashboard", key = "#email")
    public LoanApplication createDraft(String email, CreateDraftRequest req) {

        LoanApplication app = new LoanApplication();
        app.setUserEmail(email);
        app.setAmount(req.getAmount());
        app.setTenureMonths(req.getTenureMonths());
        app.setPurpose(req.getPurpose());
        app.setStatus(LoanApplicationStatus.DRAFT);
        app.setCreatedAt(LocalDateTime.now());

        repo.save(app);
        saveHistory(app.getId(), LoanApplicationStatus.DRAFT, "Draft created");

        return app;
    }

    @CacheEvict(value = "dashboard", key = "#email")
    public LoanApplication submit(UUID id, String email) {

        LoanApplication app = repo.findById(id).orElseThrow();

        if (!app.getUserEmail().equals(email))
            throw new RuntimeException("Unauthorized");

        validateTransition(app.getStatus(), LoanApplicationStatus.SUBMITTED);

        app.setStatus(LoanApplicationStatus.SUBMITTED);
        app.setSubmittedAt(LocalDateTime.now());

        repo.save(app);

        saveHistory(id, LoanApplicationStatus.SUBMITTED, "Application submitted");

        return app;
    }

    // 🔥 CALLED BY DOCUMENT SERVICE EVENT (later)
    @CacheEvict(value = "dashboard", allEntries = true)
    public LoanApplication markDocsUploaded(UUID id) {

        LoanApplication app = repo.findById(id).orElseThrow();

        validateTransition(app.getStatus(), LoanApplicationStatus.DOCS_UPLOADED);

        app.setStatus(LoanApplicationStatus.DOCS_UPLOADED);
        repo.save(app);

        saveHistory(id, LoanApplicationStatus.DOCS_UPLOADED, "Documents uploaded");

        return app;
    }

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
        repo.save(app);

        saveHistory(id, req.getStatus(), req.getRemark());
        publisher.publishDecision(app, req.getRemark());

        return app;
    }

    private void validateTransition(LoanApplicationStatus current, LoanApplicationStatus target) {

        if (current == LoanApplicationStatus.DRAFT && target == LoanApplicationStatus.SUBMITTED) return;

        if (current == LoanApplicationStatus.SUBMITTED && target == LoanApplicationStatus.DOCS_UPLOADED) return;

        if (current == LoanApplicationStatus.DOCS_UPLOADED && target == LoanApplicationStatus.UNDER_VERIFICATION) return;

        if (current == LoanApplicationStatus.UNDER_VERIFICATION && target == LoanApplicationStatus.UNDER_REVIEW) return;

        if (current == LoanApplicationStatus.UNDER_REVIEW &&
                (target == LoanApplicationStatus.APPROVED || target == LoanApplicationStatus.REJECTED)) return;

        throw new RuntimeException("Invalid state transition");
    }

    private void saveHistory(UUID id, LoanApplicationStatus status, String remark) {

        LoanStatusHistory h = new LoanStatusHistory();
        h.setApplicationId(id);
        h.setStatus(status);
        h.setRemark(remark);
        h.setTimestamp(LocalDateTime.now());

        historyRepo.save(h);
    }
}