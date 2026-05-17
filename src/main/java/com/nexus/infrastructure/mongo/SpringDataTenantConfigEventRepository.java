package com.nexus.infrastructure.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

interface SpringDataTenantConfigEventRepository extends MongoRepository<TenantConfigEventDocument, String> {

    List<TenantConfigEventDocument> findByTenantId(UUID tenantId, Sort sort);
}
