package com.finflow.application.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConsumerConfig {

    @Bean
    public Queue documentQueue() {
        return new Queue("document.queue", true);
    }

    @Bean
    public TopicExchange documentExchange() {
        return new TopicExchange("document.exchange");
    }

    @Bean
    public Binding uploadedBinding(Queue documentQueue, TopicExchange documentExchange) {
        return BindingBuilder.bind(documentQueue)
                .to(documentExchange)
                .with("document.uploaded");
    }

    @Bean
    public Binding verifiedBinding(Queue documentQueue, TopicExchange documentExchange) {
        return BindingBuilder.bind(documentQueue)
                .to(documentExchange)
                .with("document.verified");
    }
}