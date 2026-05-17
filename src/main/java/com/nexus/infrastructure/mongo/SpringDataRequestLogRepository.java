package com.nexus.infrastructure.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringDataRequestLogRepository extends MongoRepository<RequestLogDocument, String> {

    List<RequestLogDocument> findByTenantIdOrderByTimestampDesc(UUID tenantId, Pageable pageable);

    List<RequestLogDocument> findByTenantIdAndStatusCodeOrderByTimestampDesc(
            UUID tenantId, int statusCode, Pageable pageable);
}
