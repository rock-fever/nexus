package com.nexus.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataRateLimitPolicyRepository extends JpaRepository<RateLimitPolicyEntity, UUID> {
    Optional<RateLimitPolicyEntity> findByTenantId(UUID tenantId);
    void deleteByTenantId(UUID tenantId);
}
