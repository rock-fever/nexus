package com.nexus.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTenantEventProducer {

    private static final String TOPIC = "tenant.config.changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaTenantEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @SuppressWarnings("null")
    public void publish(TenantConfigChangedEvent event) {
        kafkaTemplate.send(TOPIC, event.tenantId().toString(), event);
    }
}
