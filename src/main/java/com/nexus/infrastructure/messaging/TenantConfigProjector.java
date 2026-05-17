package com.nexus.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.nexus.infrastructure.mongo.TenantConfigEventStore;

@Component
public class TenantConfigProjector {

    private final TenantConfigEventStore eventStore;

    public TenantConfigProjector(TenantConfigEventStore eventStore) {
        this.eventStore = eventStore;
    }

    @KafkaListener(topics = "tenant.config.changed", groupId = "nexus-tenant-projector")
    public void on(TenantConfigChangedEvent event) {
        eventStore.append(event);
    }
}
