package com.nexus.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.domain.model.RateLimitPolicy;
import com.nexus.domain.repository.IRateLimitPolicyRepository;

@Repository
public class JpaRateLimitPolicyRepository implements IRateLimitPolicyRepository {

    private final SpringDataRateLimitPolicyRepository springDataRepo;

    public JpaRateLimitPolicyRepository(SpringDataRateLimitPolicyRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    @SuppressWarnings("null")
    public RateLimitPolicy save(RateLimitPolicy policy) {
        return toDomain(springDataRepo.save(toEntity(policy)));
    }

    @Override
    public Optional<RateLimitPolicy> findByTenantId(UUID tenantId) {
        return springDataRepo.findByTenantId(tenantId).map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByTenantId(UUID tenantId) {
        springDataRepo.deleteByTenantId(tenantId);
    }

    private RateLimitPolicy toDomain(RateLimitPolicyEntity entity) {
        return RateLimitPolicy.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .requestsPerMinute(entity.getRequestsPerMinute())
                .build();
    }

    private RateLimitPolicyEntity toEntity(RateLimitPolicy policy) {
        return new RateLimitPolicyEntity(
                policy.getId(),
                policy.getTenantId(),
                policy.getRequestsPerMinute()
        );
    }
}
