package com.nexus.infrastructure.messaging;

import java.util.UUID;

public record RequestLoggedEvent(
        UUID tenantId,
        UUID routeId,
        String method,
        String path,
        int statusCode,
        long latencyMs,
        long timestamp) {}
