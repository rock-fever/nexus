package com.nexus.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.nexus.domain.model.RateLimitPolicy;

public interface IRateLimitPolicyRepository {
    RateLimitPolicy save(RateLimitPolicy policy);
    Optional<RateLimitPolicy> findByTenantId(UUID tenantId);
    void deleteByTenantId(UUID tenantId);
}
