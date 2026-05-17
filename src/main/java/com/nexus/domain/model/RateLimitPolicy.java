package com.nexus.domain.model;

import java.util.UUID;

public class RateLimitPolicy {
    private final UUID id;
    private final UUID tenantId;
    private final int requestsPerMinute;

    private RateLimitPolicy(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.requestsPerMinute = builder.requestsPerMinute;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public int getRequestsPerMinute() { return requestsPerMinute; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID tenantId;
        private int requestsPerMinute;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder tenantId(UUID tenantId) { this.tenantId = tenantId; return this; }
        public Builder requestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; return this; }

        public RateLimitPolicy build() { return new RateLimitPolicy(this); }
    }
}
