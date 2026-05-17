package com.nexus.api.dto;

import java.util.UUID;

import com.nexus.domain.model.enums.AuthType;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RouteResponse {
    private UUID id;
    private UUID tenantId;
    private String pathPattern;
    private String targetUrl;
    private String method;
    private boolean stripPrefix;
    private AuthType authType;
    private int retryAttempts;
    private boolean active;
}
