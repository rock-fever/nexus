package com.nexus.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.nexus.domain.model.Tenant;
import com.nexus.domain.model.enums.TenantStatus;
import com.nexus.domain.repository.ITenantRepository;

@Repository
public class JpaTenantRepository implements ITenantRepository {

    private final SpringDataTenantRepository springDataRepo;

    public JpaTenantRepository(SpringDataTenantRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    @SuppressWarnings("null")
    public Optional<Tenant> findById(UUID id) {
        return springDataRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        return springDataRepo.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public List<Tenant> findAll(int page, int size) {
        return springDataRepo.findAll(PageRequest.of(page, size))
                .map(this::toDomain)
                .toList();
    }

    @Override
    @SuppressWarnings("null")
    public Tenant save(Tenant tenant) {
        return toDomain(springDataRepo.save(toEntity(tenant)));
    }

    @Override
    @SuppressWarnings("null")
    public Tenant updateStatus(UUID id, TenantStatus status) {
        TenantEntity entity = springDataRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        entity.setStatus(status);
        return toDomain(springDataRepo.save(entity));
    }

    private Tenant toDomain(TenantEntity entity) {
        return Tenant.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .plan(entity.getPlan())
                .tenantStatus(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private TenantEntity toEntity(Tenant tenant) {
        return new TenantEntity(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getPlan(),
                tenant.getTenantStatus(),
                tenant.getCreatedAt()
        );
    }
}
