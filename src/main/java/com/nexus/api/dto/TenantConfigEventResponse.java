package com.nexus.api.dto;

import java.util.UUID;

public record TenantConfigEventResponse(UUID tenantId, String eventType, String payload, long timestamp) {}
