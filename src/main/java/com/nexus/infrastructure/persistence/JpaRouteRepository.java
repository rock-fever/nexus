package com.nexus.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.nexus.domain.model.Route;
import com.nexus.domain.repository.IRouteRepository;

@Repository
public class JpaRouteRepository implements IRouteRepository {

    private final SpringDataRouteRepository springDataRepo;

    public JpaRouteRepository(SpringDataRouteRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    @SuppressWarnings("null")
    public Route save(Route route) {
        return toDomain(springDataRepo.save(toEntity(route)));
    }

    @Override
    @SuppressWarnings("null")
    public Optional<Route> findById(UUID id) {
        return springDataRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Route> findByTenantId(UUID tenantId) {
        return springDataRepo.findByTenantId(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @SuppressWarnings("null")
    public void deleteById(UUID id) {
        springDataRepo.deleteById(id);
    }

    private Route toDomain(RouteEntity entity) {
        return Route.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .pathPattern(entity.getPathPattern())
                .targetUrl(entity.getTargetUrl())
                .method(entity.getMethod())
                .stripPrefix(entity.isStripPrefix())
                .authType(entity.getAuthType())
                .retryAttempts(entity.getRetryAttempts())
                .active(entity.isActive())
                .build();
    }

    private RouteEntity toEntity(Route route) {
        return new RouteEntity(
                route.getId(),
                route.getTenantId(),
                route.getPathPattern(),
                route.getTargetUrl(),
                route.getMethod(),
                route.isStripPrefix(),
                route.getAuthType(),
                route.getRetryAttempts(),
                route.isActive()
        );
    }
}
