package com.nexus.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

interface SpringDataApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {
    Optional<ApiKeyEntity> findByKeyHash(String keyHash);
    List<ApiKeyEntity> findByTenantId(UUID tenantId);

    @Modifying
    @Transactional
    @Query("UPDATE ApiKeyEntity a SET a.active = false WHERE a.id = :id")
    void revokeById(UUID id);
}
