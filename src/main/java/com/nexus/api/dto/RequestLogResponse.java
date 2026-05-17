package com.nexus.api.dto;

import java.util.UUID;

public record RequestLogResponse(
        UUID tenantId,
        UUID routeId,
        String method,
        String path,
        int statusCode,
        long latencyMs,
        long timestamp) {}
