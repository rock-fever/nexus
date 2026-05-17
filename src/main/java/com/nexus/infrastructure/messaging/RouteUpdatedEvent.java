package com.nexus.infrastructure.messaging;

import java.util.UUID;

public record RouteUpdatedEvent(UUID routeId, UUID tenantId, String eventType) {}
