package com.nexus.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexus.domain.model.ApiKey;

public interface IApiKeyRepository {
    ApiKey save(ApiKey apiKey);
    Optional<ApiKey> findByKeyHash(String keyHash);
    List<ApiKey> findByTenantId(UUID tenantId);
    void revokeById(UUID id);
}
