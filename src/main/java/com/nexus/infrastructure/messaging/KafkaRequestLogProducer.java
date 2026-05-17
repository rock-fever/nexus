package com.nexus.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaRequestLogProducer {

    private static final String TOPIC = "request.logged";

    private final KafkaTemplate<String, RequestLoggedEvent> kafkaTemplate;

    public KafkaRequestLogProducer(KafkaTemplate<String, RequestLoggedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @SuppressWarnings("null")
    public void publish(RequestLoggedEvent event) {
        kafkaTemplate.send(TOPIC, event.tenantId().toString(), event);
    }
}
