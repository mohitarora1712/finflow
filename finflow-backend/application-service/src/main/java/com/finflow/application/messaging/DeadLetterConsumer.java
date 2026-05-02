package com.finflow.application.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    // NEW CHANGE: Log failed messages that end up in DLQ
    @RabbitListener(queues = "document.dlq")
    public void processFailedMessages(Map<String, Object> message) {
        log.error("FAILED MESSAGE RECEIVED IN DLQ: {}", message);
        // Here you could send an email alert or save to an error_logs table
    }
}
