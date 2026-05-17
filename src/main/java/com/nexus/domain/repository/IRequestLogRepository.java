package com.nexus.domain.repository;

import java.util.List;
import java.util.UUID;

import com.nexus.domain.model.RequestLog;

public interface IRequestLogRepository {
    void save(RequestLog log);
    List<RequestLog> findByTenantId(UUID tenantId, int limit, Integer statusCode);
}
