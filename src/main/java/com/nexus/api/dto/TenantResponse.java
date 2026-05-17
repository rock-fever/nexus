package com.nexus.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nexus.domain.model.enums.Plan;
import com.nexus.domain.model.enums.TenantStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TenantResponse {
    private UUID id; 
    private String name;
    private String slug;
    private Plan plan;
    private TenantStatus status;
    private LocalDateTime createdAt;
}
