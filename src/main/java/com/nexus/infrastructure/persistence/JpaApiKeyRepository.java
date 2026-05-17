package com.nexus.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.nexus.domain.model.ApiKey;
import com.nexus.domain.repository.IApiKeyRepository;

@Repository
public class JpaApiKeyRepository implements IApiKeyRepository {

    private final SpringDataApiKeyRepository springDataRepo;

    public JpaApiKeyRepository(SpringDataApiKeyRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    @SuppressWarnings("null")
    public ApiKey save(ApiKey apiKey) {
        return toDomain(springDataRepo.save(toEntity(apiKey)));
    }

    @Override
    public Optional<ApiKey> findByKeyHash(String keyHash) {
        return springDataRepo.findByKeyHash(keyHash).map(this::toDomain);
    }

    @Override
    public List<ApiKey> findByTenantId(UUID tenantId) {
        return springDataRepo.findByTenantId(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void revokeById(UUID id) {
        springDataRepo.revokeById(id);
    }

    private ApiKey toDomain(ApiKeyEntity entity) {
        return ApiKey.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .keyHash(entity.getKeyHash())
                .name(entity.getName())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private ApiKeyEntity toEntity(ApiKey apiKey) {
        return new ApiKeyEntity(
                apiKey.getId(),
                apiKey.getTenantId(),
                apiKey.getKeyHash(),
                apiKey.getName(),
                apiKey.isActive(),
                apiKey.getCreatedAt()
        );
    }
}
