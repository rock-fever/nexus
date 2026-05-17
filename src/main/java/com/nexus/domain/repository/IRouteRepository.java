package com.nexus.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexus.domain.model.Route;

public interface IRouteRepository {
    Route save(Route route);
    Optional<Route> findById(UUID id);
    List<Route> findByTenantId(UUID tenantId);
    void deleteById(UUID id);
}
