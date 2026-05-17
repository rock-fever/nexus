package com.nexus.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rate_limit_policies")
public class RateLimitPolicyEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    @Column(name = "requests_per_minute", nullable = false)
    private int requestsPerMinute;

    protected RateLimitPolicyEntity() {}

    public RateLimitPolicyEntity(UUID id, UUID tenantId, int requestsPerMinute) {
        this.id = id;
        this.tenantId = tenantId;
        this.requestsPerMinute = requestsPerMinute;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public int getRequestsPerMinute() { return requestsPerMinute; }
}
