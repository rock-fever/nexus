package com.nexus.infrastructure.mongo;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tenant_config_events")
class TenantConfigEventDocument {

    @Id
    private String id;

    @Indexed
    private UUID tenantId;
    private String eventType;
    private String payload;
    private long timestamp;

    TenantConfigEventDocument() {}

    TenantConfigEventDocument(UUID tenantId, String eventType, String payload, long timestamp) {
        this.tenantId = tenantId;
        this.eventType = eventType;
        this.payload = payload;
        this.timestamp = timestamp;
    }

    UUID getTenantId() { return tenantId; }
    String getEventType() { return eventType; }
    String getPayload() { return payload; }
    long getTimestamp() { return timestamp; }
}
