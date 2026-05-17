package com.nexus.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaRouteEventProducer {

    private static final String TOPIC = "route.updated";

    private final KafkaTemplate<String, RouteUpdatedEvent> kafkaTemplate;

    public KafkaRouteEventProducer(KafkaTemplate<String, RouteUpdatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @SuppressWarnings("null")
    public void publish(RouteUpdatedEvent event) {
        // Partition by tenantId so all events for a tenant go to the same partition.
        kafkaTemplate.send(TOPIC, event.tenantId().toString(), event);
    }
}
