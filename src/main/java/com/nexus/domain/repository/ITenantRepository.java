package com.nexus.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexus.domain.model.Tenant;
import com.nexus.domain.model.enums.TenantStatus;

public interface ITenantRepository {
    Optional<Tenant> findById (UUID id);
    Optional<Tenant> findBySlug (String slug);
    List<Tenant> findAll(int page, int size);
    Tenant save (Tenant tenant);
    Tenant updateStatus (UUID id, TenantStatus tenantStatusToUpdate);
}
