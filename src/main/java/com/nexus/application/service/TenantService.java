package com.nexus.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.api.dto.CreateTenantRequest;
import com.nexus.api.dto.TenantResponse;
import com.nexus.domain.model.Tenant;
import com.nexus.domain.model.enums.TenantStatus;
import com.nexus.domain.repository.ITenantRepository;
import com.nexus.infrastructure.messaging.KafkaTenantEventProducer;
import com.nexus.infrastructure.messaging.TenantConfigChangedEvent;
import com.nexus.shared.exception.TenantNotFoundException;

@Service
public class TenantService {

    private final ITenantRepository tenantRepository;
    private final KafkaTenantEventProducer tenantEventProducer;
    private final ObjectMapper objectMapper;

    public TenantService(ITenantRepository tenantRepository,
                         KafkaTenantEventProducer tenantEventProducer,
                         ObjectMapper objectMapper) {
        this.tenantRepository = tenantRepository;
        this.tenantEventProducer = tenantEventProducer;
        this.objectMapper = objectMapper;
    }

    public TenantResponse createTenant(CreateTenantRequest request) {
        Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .slug(request.getSlug())
                .plan(request.getPlan())
                .tenantStatus(TenantStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        Tenant saved = tenantRepository.save(tenant);
        TenantResponse response = toResponse(saved);
        publishEvent(saved.getId(), "TENANT_CREATED", response);
        return response;
    }

    public TenantResponse getTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException(id));

        return toResponse(tenant);
    }

    public List<TenantResponse> listTenants(int page, int size) {
        return tenantRepository.findAll(page, size).stream()
                .map(this::toResponse)
                .toList();
    }

    public TenantResponse updateStatus(UUID id, TenantStatus status) {
        Tenant updated = tenantRepository.updateStatus(id, status);
        TenantResponse response = toResponse(updated);
        publishEvent(id, "STATUS_CHANGED", response);
        return response;
    }

    private void publishEvent(UUID tenantId, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            tenantEventProducer.publish(
                    new TenantConfigChangedEvent(tenantId, eventType, json, System.currentTimeMillis()));
        } catch (JsonProcessingException ignored) {
            // Serialization failure should not fail the main operation
        }
    }

    private TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .slug(tenant.getSlug())
                .plan(tenant.getPlan())
                .status(tenant.getTenantStatus())
                .createdAt(tenant.getCreatedAt())
                .build();
    }
}
