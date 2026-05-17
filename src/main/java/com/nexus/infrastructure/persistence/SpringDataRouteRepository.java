package com.nexus.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataRouteRepository extends JpaRepository<RouteEntity, UUID> {
    List<RouteEntity> findByTenantId(UUID tenantId);
}
