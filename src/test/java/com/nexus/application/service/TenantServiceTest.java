package com.nexus.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.nexus.AbstractIntegrationTest;
import com.nexus.api.dto.CreateTenantRequest;
import com.nexus.api.dto.TenantResponse;
import com.nexus.domain.model.enums.Plan;
import com.nexus.domain.model.enums.TenantStatus;
import com.nexus.shared.exception.TenantNotFoundException;

class TenantServiceTest extends AbstractIntegrationTest {

    @Autowired
    private TenantService tenantService;

    @Test
    void createTenant_persistsAndReturnsResponse() {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Acme Corp")
                .slug("acme-" + UUID.randomUUID())
                .plan(Plan.PRO)
                .build();

        TenantResponse response = tenantService.createTenant(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("Acme Corp");
        assertThat(response.getPlan()).isEqualTo(Plan.PRO);
        assertThat(response.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void getTenant_returnsPersistedTenant() {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Beta Inc")
                .slug("beta-" + UUID.randomUUID())
                .plan(Plan.FREE)
                .build();
        TenantResponse created = tenantService.createTenant(request);

        TenantResponse fetched = tenantService.getTenant(created.getId());

        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getSlug()).isEqualTo(created.getSlug());
    }

    @Test
    void getTenant_throwsWhenNotFound() {
        assertThatThrownBy(() -> tenantService.getTenant(UUID.randomUUID()))
                .isInstanceOf(TenantNotFoundException.class);
    }

    @Test
    void updateStatus_changesToSuspended() {
        CreateTenantRequest request = CreateTenantRequest.builder()
                .name("Gamma LLC")
                .slug("gamma-" + UUID.randomUUID())
                .plan(Plan.ENTERPRISE)
                .build();
        TenantResponse created = tenantService.createTenant(request);

        TenantResponse updated = tenantService.updateStatus(created.getId(), TenantStatus.SUSPENDED);

        assertThat(updated.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
    }
}
