package com.nexus.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexus.domain.model.Route;
import com.nexus.domain.repository.IRouteQueryRepository;

@Service
public class RouteQueryService {

    private final IRouteQueryRepository routeQueryRepository;

    public RouteQueryService(IRouteQueryRepository routeQueryRepository) {
        this.routeQueryRepository = routeQueryRepository;
    }

    public List<Route> findRoutesForTenant(UUID tenantId) {
        return routeQueryRepository.findByTenantId(tenantId);
    }
}
