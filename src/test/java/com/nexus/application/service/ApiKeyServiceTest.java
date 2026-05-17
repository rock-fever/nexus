package com.nexus.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.nexus.AbstractIntegrationTest;
import com.nexus.api.dto.ApiKeyResponse;
import com.nexus.api.dto.CreateApiKeyRequest;
import com.nexus.api.dto.CreateTenantRequest;
import com.nexus.domain.model.enums.Plan;

class ApiKeyServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private TenantService tenantService;

    @Test
    void createApiKey_returnsRawKeyOnce() {
        UUID tenantId = createTenant();

        ApiKeyResponse response = apiKeyService.createApiKey(tenantId,
                CreateApiKeyRequest.builder().name("my-key").build());

        assertThat(response.getId()).isNotNull();
        assertThat(response.getRawKey()).isNotNull().isNotBlank();
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void listApiKeys_doesNotExposeRawKey() {
        UUID tenantId = createTenant();
        apiKeyService.createApiKey(tenantId, CreateApiKeyRequest.builder().name("list-key").build());

        List<ApiKeyResponse> keys = apiKeyService.listApiKeys(tenantId);

        assertThat(keys).isNotEmpty();
        assertThat(keys).allMatch(k -> k.getRawKey() == null);
    }

    @Test
    void revokeApiKey_deactivatesKey() {
        UUID tenantId = createTenant();
        ApiKeyResponse created = apiKeyService.createApiKey(tenantId,
                CreateApiKeyRequest.builder().name("revoke-me").build());

        apiKeyService.revokeApiKey(created.getId());

        List<ApiKeyResponse> keys = apiKeyService.listApiKeys(tenantId);
        assertThat(keys)
                .filteredOn(k -> k.getId().equals(created.getId()))
                .allMatch(k -> !k.isActive());
    }

    private UUID createTenant() {
        return tenantService.createTenant(CreateTenantRequest.builder()
                .name("KeyTest")
                .slug("key-test-" + UUID.randomUUID())
                .plan(Plan.FREE)
                .build()).getId();
    }
}
