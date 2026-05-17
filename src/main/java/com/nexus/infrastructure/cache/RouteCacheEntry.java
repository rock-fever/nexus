package com.nexus.infrastructure.cache;

import java.util.UUID;

import com.nexus.domain.model.enums.AuthType;

// Redis serialisation DTO — mirrors Route fields without touching the domain model.
record RouteCacheEntry(
        UUID id,
        UUID tenantId,
        String pathPattern,
        String targetUrl,
        String method,
        boolean stripPrefix,
        AuthType authType,
        int retryAttempts,
        boolean active) {}
