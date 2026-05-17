package com.nexus.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nexus.domain.model.enums.Plan;
import com.nexus.domain.model.enums.TenantStatus;

public class Tenant {
    private final UUID id;
    private final String name;
    private final String slug;
    private final Plan plan;
    private final TenantStatus tenantStatus;
    private final LocalDateTime createdAt;

    private Tenant(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.slug = builder.slug;
        this.plan = builder.plan;
        this.tenantStatus = builder.tenantStatus;
        this.createdAt = builder.createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public Plan getPlan() { return plan; }
    public TenantStatus getTenantStatus() { return tenantStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String name;
        private String slug;
        private Plan plan;
        private TenantStatus tenantStatus;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder slug(String slug) { this.slug = slug; return this; }
        public Builder plan(Plan plan) { this.plan = plan; return this; }
        public Builder tenantStatus(TenantStatus tenantStatus) { this.tenantStatus = tenantStatus; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Tenant build() { return new Tenant(this); }
    }
}

