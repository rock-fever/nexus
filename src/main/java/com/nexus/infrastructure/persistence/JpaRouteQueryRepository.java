package com.nexus.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.nexus.domain.model.Route;
import com.nexus.domain.repository.IRouteQueryRepository;

@Repository
@Qualifier("jpaRouteQueryRepository")
public class JpaRouteQueryRepository implements IRouteQueryRepository {

    private final SpringDataRouteRepository springDataRepo;

    public JpaRouteQueryRepository(SpringDataRouteRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public List<Route> findByTenantId(UUID tenantId) {
        return springDataRepo.findByTenantId(tenantId).stream()
                .map(this::toDomain)
                .toList();
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
}
