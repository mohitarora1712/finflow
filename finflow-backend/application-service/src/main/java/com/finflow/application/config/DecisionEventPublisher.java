package com.finflow.application.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.finflow.application.entity.LoanApplication;

@Component
public class DecisionEventPublisher {

 private final RabbitTemplate rabbitTemplate;

 public DecisionEventPublisher(RabbitTemplate rabbitTemplate) {
  this.rabbitTemplate = rabbitTemplate;
 }

 public void publishDecision(LoanApplication app, String remark) {

  Map<String,Object> event = new HashMap<>();
  event.put("applicationId", app.getId());
  event.put("userEmail", app.getUserEmail());
  event.put("status", app.getStatus().name());
  event.put("remark", remark);

  rabbitTemplate.convertAndSend(
    "application.exchange",
    "application.decision",
    event
  );
 }

}
