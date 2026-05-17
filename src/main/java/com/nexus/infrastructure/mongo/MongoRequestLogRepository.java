package com.nexus.infrastructure.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.nexus.domain.model.RequestLog;
import com.nexus.domain.repository.IRequestLogRepository;

@Repository
public class MongoRequestLogRepository implements IRequestLogRepository {

    private final SpringDataRequestLogRepository springDataRepo;

    public MongoRequestLogRepository(SpringDataRequestLogRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public void save(RequestLog log) {
        springDataRepo.save(new RequestLogDocument(
                log.getTenantId(), log.getRouteId(), log.getMethod(), log.getPath(),
                log.getStatusCode(), log.getLatencyMs(), log.getTimestamp()));
    }

    @Override
    public List<RequestLog> findByTenantId(UUID tenantId, int limit, Integer statusCode) {
        PageRequest page = PageRequest.of(0, limit);
        List<RequestLogDocument> docs = statusCode != null
                ? springDataRepo.findByTenantIdAndStatusCodeOrderByTimestampDesc(tenantId, statusCode, page)
                : springDataRepo.findByTenantIdOrderByTimestampDesc(tenantId, page);

        return docs.stream().map(this::toDomain).toList();
    }

    private RequestLog toDomain(RequestLogDocument doc) {
        return new RequestLog(
                doc.getTenantId(), doc.getRouteId(), doc.getMethod(), doc.getPath(),
                doc.getStatusCode(), doc.getLatencyMs(), doc.getTimestamp());
    }
}
