package com.nexus.domain.repository;

import java.util.List;
import java.util.UUID;

import com.nexus.domain.model.Route;

public interface IRouteQueryRepository {
    List<Route> findByTenantId(UUID tenantId);
}
