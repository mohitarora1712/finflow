package com.finflow.admin.service;

import com.finflow.admin.client.ApplicationClient;
import com.finflow.admin.client.DocumentClient;
import com.finflow.admin.dto.AdminDecisionRequest;
import com.finflow.admin.dto.LoanApplicationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminWorkflowServiceTest {

    @Mock
    private ApplicationClient appClient;

    @Mock
    private DocumentClient docClient;

    @InjectMocks
    private AdminWorkflowService service;

    @Test
    void getAllApps_ShouldReturnList() {
        when(appClient.getAll()).thenReturn(Collections.emptyList());

        List<LoanApplicationResponse> result = service.getAllApps();

        assertNotNull(result);
        verify(appClient).getAll();
    }

    @Test
    void fallbackApps_ShouldReturnEmptyList() {
        List<LoanApplicationResponse> result = service.fallbackApps(new RuntimeException("Test Error"));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void verifyDoc_ShouldCallDocClient() {
        UUID docId = UUID.randomUUID();
        service.verifyDoc(docId);
        verify(docClient).verify(docId);
    }

    @Test
    void decide_ShouldCallAppClient() {
        UUID appId = UUID.randomUUID();
        AdminDecisionRequest req = new AdminDecisionRequest();
        service.decide(appId, req);
        verify(appClient).decide(appId, req);
    }
}
