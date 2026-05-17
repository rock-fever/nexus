package com.nexus.infrastructure.mongo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.nexus.infrastructure.messaging.TenantConfigChangedEvent;

@Repository
public class TenantConfigEventStore {

    private final SpringDataTenantConfigEventRepository repository;

    public TenantConfigEventStore(SpringDataTenantConfigEventRepository repository) {
        this.repository = repository;
    }

    public void append(TenantConfigChangedEvent event) {
        repository.save(new TenantConfigEventDocument(
                event.tenantId(), event.eventType(), event.payload(), event.timestamp()));
    }

    public List<TenantConfigChangedEvent> findByTenantId(UUID tenantId) {
        return repository.findByTenantId(tenantId, Sort.by(Sort.Direction.ASC, "timestamp"))
                .stream()
                .map(doc -> new TenantConfigChangedEvent(
                        doc.getTenantId(), doc.getEventType(), doc.getPayload(), doc.getTimestamp()))
                .toList();
    }
}
