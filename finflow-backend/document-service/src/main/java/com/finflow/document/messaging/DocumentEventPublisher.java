package com.finflow.document.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DocumentEventPublisher {

 private final RabbitTemplate rabbit;

 public DocumentEventPublisher(RabbitTemplate rabbit) {
  this.rabbit = rabbit;
 }

 public void publishUploaded(UUID appId) {

  Map<String,Object> event = new HashMap<>();
  event.put("applicationId", appId.toString());
  event.put("eventType", "DOCUMENT_UPLOADED");

  rabbit.convertAndSend(
   "document.exchange",
   "document.uploaded",
   event
  );
 }

 public void publishVerified(UUID appId) {

  Map<String,Object> event = new HashMap<>();
  event.put("applicationId", appId.toString());
  event.put("eventType", "DOCUMENT_VERIFIED");

  rabbit.convertAndSend(
   "document.exchange",
   "document.verified",
   event
  );
 }

}

