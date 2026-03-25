package com.finflow.application.messaging;

import com.finflow.application.service.LoanApplicationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class DocumentEventConsumer {

    private final LoanApplicationService service;

    public DocumentEventConsumer(LoanApplicationService service) {
        this.service = service;
    }

    @RabbitListener(queues = "document.queue")
    public void handleEvent(Map<String, Object> event) {

        String eventType = (String) event.get("eventType");
        UUID appId = UUID.fromString((String) event.get("applicationId"));

        switch (eventType) {

            case "DOCUMENT_UPLOADED":
                service.markDocsUploaded(appId);
                break;

            case "DOCUMENT_VERIFIED":
                service.markUnderVerification(appId);
                service.moveToReview(appId);
                break;
        }
    }
}