package com.nexus.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ApiKey {
    private final UUID id;
    private final UUID tenantId;
    private final String keyHash;
    private final String name;
    private final boolean active;
    private final LocalDateTime createdAt;

    private ApiKey(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.keyHash = builder.keyHash;
        this.name = builder.name;
        this.active = builder.active;
        this.createdAt = builder.createdAt;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getKeyHash() { return keyHash; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID tenantId;
        private String keyHash;
        private String name;
        private boolean active = true;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder tenantId(UUID tenantId) { this.tenantId = tenantId; return this; }
        public Builder keyHash(String keyHash) { this.keyHash = keyHash; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ApiKey build() { return new ApiKey(this); }
    }
}
