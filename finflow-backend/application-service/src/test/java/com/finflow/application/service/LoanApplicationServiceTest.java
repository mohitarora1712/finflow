package com.finflow.application.service;

import com.finflow.application.config.DecisionEventPublisher;
import com.finflow.application.context.IdentityContext;
import com.finflow.application.dto.AdminDecisionRequest;
import com.finflow.application.dto.CreateDraftRequest;
import com.finflow.application.entity.LoanApplication;
import com.finflow.application.entity.LoanApplicationStatus;
import com.finflow.application.repository.LoanApplicationRepository;
import com.finflow.application.repository.StatusHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository repo;

    @Mock
    private StatusHistoryRepository historyRepo;

    @Mock
    private DecisionEventPublisher publisher;

    @InjectMocks
    private LoanApplicationService service;

    private MockedStatic<IdentityContext> mockedIdentity;

    @BeforeEach
    void setUp() {
        mockedIdentity = mockStatic(IdentityContext.class);
    }

    @AfterEach
    void tearDown() {
        mockedIdentity.close();
    }

    @Test
    void getUserApps_ShouldReturnList() {
        String email = "test@example.com";
        mockedIdentity.when(IdentityContext::getEmail).thenReturn(email);
        when(repo.findByUserEmail(email)).thenReturn(Collections.singletonList(new LoanApplication()));

        List<LoanApplication> result = service.getUserApps();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repo).findByUserEmail(email);
    }

    @Test
    void createDraft_ShouldSaveAndReturn() {
        String email = "test@example.com";
        mockedIdentity.when(IdentityContext::getEmail).thenReturn(email);
        
        CreateDraftRequest req = new CreateDraftRequest();
        req.setAmount(1000.0);
        req.setTenureMonths(12);
        req.setPurpose("Test Purpose");

        LoanApplication result = service.createDraft(req);

        assertNotNull(result);
        assertEquals(email, result.getUserEmail());
        assertEquals(1000.0, result.getAmount());
        assertEquals(LoanApplicationStatus.DRAFT, result.getStatus());
        verify(repo).save(any(LoanApplication.class));
        verify(historyRepo).save(any());
    }

    @Test
    void submit_ShouldChangeStatus() {
        UUID id = UUID.randomUUID();
        String email = "test@example.com";
        mockedIdentity.when(IdentityContext::getEmail).thenReturn(email);

        LoanApplication app = new LoanApplication();
        app.setId(id);
        app.setUserEmail(email);
        app.setStatus(LoanApplicationStatus.DRAFT);

        when(repo.findById(id)).thenReturn(Optional.of(app));

        LoanApplication result = service.submit(id);

        assertEquals(LoanApplicationStatus.SUBMITTED, result.getStatus());
        verify(repo).save(app);
    }

    @Test
    void submit_ShouldThrow_WhenNotOwner() {
        UUID id = UUID.randomUUID();
        mockedIdentity.when(IdentityContext::getEmail).thenReturn("other@example.com");

        LoanApplication app = new LoanApplication();
        app.setUserEmail("owner@example.com");

        when(repo.findById(id)).thenReturn(Optional.of(app));

        assertThrows(RuntimeException.class, () -> service.submit(id));
    }

    @Test
    void markDocsUploaded_ShouldChangeStatus() {
        UUID id = UUID.randomUUID();
        LoanApplication app = new LoanApplication();
        app.setStatus(LoanApplicationStatus.SUBMITTED);

        when(repo.findById(id)).thenReturn(Optional.of(app));

        LoanApplication result = service.markDocsUploaded(id);

        assertEquals(LoanApplicationStatus.DOCS_UPLOADED, result.getStatus());
        verify(repo).save(app);
    }

    @Test
    void markUnderVerification_ShouldChangeStatus() {
        UUID id = UUID.randomUUID();
        LoanApplication app = new LoanApplication();
        app.setStatus(LoanApplicationStatus.DOCS_UPLOADED);

        when(repo.findById(id)).thenReturn(Optional.of(app));

        LoanApplication result = service.markUnderVerification(id);

        assertEquals(LoanApplicationStatus.UNDER_VERIFICATION, result.getStatus());
        verify(repo).save(app);
    }

    @Test
    void moveToReview_ShouldChangeStatus() {
        UUID id = UUID.randomUUID();
        LoanApplication app = new LoanApplication();
        app.setStatus(LoanApplicationStatus.UNDER_VERIFICATION);

        when(repo.findById(id)).thenReturn(Optional.of(app));

        LoanApplication result = service.moveToReview(id);

        assertEquals(LoanApplicationStatus.UNDER_REVIEW, result.getStatus());
        verify(repo).save(app);
    }

    @Test
    void decision_ShouldApprove() {
        UUID id = UUID.randomUUID();
        LoanApplication app = new LoanApplication();
        app.setStatus(LoanApplicationStatus.UNDER_REVIEW);

        AdminDecisionRequest req = new AdminDecisionRequest();
        req.setStatus(LoanApplicationStatus.APPROVED);
        req.setRemark("Looks good");

        when(repo.findById(id)).thenReturn(Optional.of(app));

        LoanApplication result = service.decision(id, req);

        assertEquals(LoanApplicationStatus.APPROVED, result.getStatus());
        assertEquals("Looks good", result.getRemark());
        verify(publisher).publishDecision(any(), any());
    }

    @Test
    void getAll_ShouldReturnAll() {
        service.getAll();
        verify(repo).findAll();
    }
}
