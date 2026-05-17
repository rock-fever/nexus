package com.nexus.infrastructure.messaging;

import java.util.UUID;

public record TenantConfigChangedEvent(UUID tenantId, String eventType, String payload, long timestamp) {}
