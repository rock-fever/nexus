package com.nexus.infrastructure.resilience;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nexus.domain.model.enums.Plan;

@Component
public class TenantBulkhead {

    private final int freeMax;
    private final int proMax;
    private final int enterpriseMax;

    private final ConcurrentHashMap<UUID, Semaphore> semaphores = new ConcurrentHashMap<>();

    public TenantBulkhead(
            @Value("${nexus.resilience.bulkhead.free-plan-max-concurrent}") int freeMax,
            @Value("${nexus.resilience.bulkhead.pro-plan-max-concurrent}") int proMax,
            @Value("${nexus.resilience.bulkhead.enterprise-plan-max-concurrent}") int enterpriseMax) {
        this.freeMax = freeMax;
        this.proMax = proMax;
        this.enterpriseMax = enterpriseMax;
    }

    // Returns true if a permit was acquired, false if the bulkhead is full.
    public boolean tryAcquire(UUID tenantId, Plan plan) {
        Semaphore semaphore = semaphores.computeIfAbsent(tenantId, id -> new Semaphore(capacityFor(plan)));
        return semaphore.tryAcquire();
    }

    public void release(UUID tenantId) {
        Semaphore semaphore = semaphores.get(tenantId);
        if (semaphore != null) {
            semaphore.release();
        }
    }

    private int capacityFor(Plan plan) {
        return switch (plan) {
            case FREE -> freeMax;
            case PRO -> proMax;
            case ENTERPRISE -> enterpriseMax;
        };
    }
}
