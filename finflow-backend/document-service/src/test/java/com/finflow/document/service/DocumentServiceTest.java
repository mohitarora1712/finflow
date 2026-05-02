package com.finflow.document.service;

import com.finflow.document.context.IdentityContext;
import com.finflow.document.entity.Document;
import com.finflow.document.entity.DocumentStatus;
import com.finflow.document.messaging.DocumentEventPublisher;
import com.finflow.document.repository.DocumentRepository;
import com.finflow.document.storage.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository repo;

    @Mock
    private FileStorageService storage;

    @Mock
    private DocumentEventPublisher publisher;

    @InjectMocks
    private DocumentService service;

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
    void upload_ShouldSaveDocument() {
        UUID appId = UUID.randomUUID();
        MultipartFile file = mock(MultipartFile.class);
        mockedIdentity.when(IdentityContext::getEmail).thenReturn("test@example.com");
        when(storage.store(eq(appId), any())).thenReturn("path/to/file");

        Document result = service.upload(appId, "ID", file);

        assertNotNull(result);
        assertEquals(appId, result.getApplicationId());
        assertEquals("ID", result.getDocType());
        assertEquals(DocumentStatus.UPLOADED, result.getStatus());
        verify(repo).save(any(Document.class));
        verify(publisher).publishUploaded(appId);
    }

    @Test
    void verify_ShouldUpdateStatus() {
        UUID docId = UUID.randomUUID();
        Document doc = new Document();
        doc.setApplicationId(UUID.randomUUID());
        
        when(repo.findById(docId)).thenReturn(Optional.of(doc));

        Document result = service.verify(docId);

        assertEquals(DocumentStatus.VERIFIED, result.getStatus());
        assertNotNull(result.getVerifiedAt());
        verify(repo).save(doc);
        verify(publisher).publishVerified(doc.getApplicationId());
    }

    @Test
    void getDocumentsByApplicationId_ShouldReturnList() {
        UUID appId = UUID.randomUUID();
        when(repo.findByApplicationId(appId)).thenReturn(Collections.emptyList());

        List<Document> result = service.getDocumentsByApplicationId(appId);

        assertNotNull(result);
        verify(repo).findByApplicationId(appId);
    }

    @Test
    void download_ShouldThrow_WhenUserNotOwner() {
        UUID docId = UUID.randomUUID();
        Document doc = new Document();
        
        mockedIdentity.when(IdentityContext::getRole).thenReturn("USER");
        mockedIdentity.when(IdentityContext::getEmail).thenReturn(null);
        when(repo.findById(docId)).thenReturn(Optional.of(doc));

        assertThrows(RuntimeException.class, () -> service.download(docId));
    }
}
