package com.nexus.api.dto;

import com.nexus.domain.model.enums.AuthType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@Getter
public class CreateRouteRequest {
    @NotBlank
    private String pathPattern;

    @NotBlank
    private String targetUrl;

    @NotBlank
    private String method;

    private boolean stripPrefix;

    @NotNull
    private AuthType authType;

    private int retryAttempts; 
}
