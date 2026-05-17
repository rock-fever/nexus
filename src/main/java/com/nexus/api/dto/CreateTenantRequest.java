package com.nexus.api.dto;

import com.nexus.domain.model.enums.Plan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class CreateTenantRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    @NotNull
    private Plan plan;
}

