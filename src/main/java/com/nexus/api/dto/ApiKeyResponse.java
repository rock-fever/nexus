package com.nexus.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiKeyResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private boolean active;
    private LocalDateTime createdAt;
    private String rawKey;
}
