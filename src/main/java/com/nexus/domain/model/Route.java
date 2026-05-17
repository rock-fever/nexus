package com.nexus.domain.model;

import java.util.UUID;

import com.nexus.domain.model.enums.AuthType;

public class Route {
    private final UUID id;
    private final UUID tenantId;
    private final String pathPattern;
    private final String targetUrl;
    private final String method;
    private final boolean stripPrefix;
    private final AuthType authType;
    private final int retryAttempts;
    private final boolean active;

    private Route(Builder builder) {
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.pathPattern = builder.pathPattern;
        this.targetUrl = builder.targetUrl;
        this.method = builder.method;
        this.stripPrefix = builder.stripPrefix;
        this.authType = builder.authType;
        this.retryAttempts = builder.retryAttempts;
        this.active = builder.active;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getPathPattern() { return pathPattern; }
    public String getTargetUrl() { return targetUrl; }
    public String getMethod() { return method; }
    public boolean isStripPrefix() { return stripPrefix; }
    public AuthType getAuthType() { return authType; }
    public int getRetryAttempts() { return retryAttempts; }
    public boolean isActive() { return active; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private UUID tenantId;
        private String pathPattern;
        private String targetUrl;
        private String method;
        private boolean stripPrefix;
        private AuthType authType;
        private int retryAttempts;
        private boolean active = true;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder tenantId(UUID tenantId) { this.tenantId = tenantId; return this; }
        public Builder pathPattern(String pathPattern) { this.pathPattern = pathPattern; return this; }
        public Builder targetUrl(String targetUrl) { this.targetUrl = targetUrl; return this; }
        public Builder method(String method) { this.method = method; return this; }
        public Builder stripPrefix(boolean stripPrefix) { this.stripPrefix = stripPrefix; return this; }
        public Builder authType(AuthType authType) { this.authType = authType; return this; }
        public Builder retryAttempts(int retryAttempts) { this.retryAttempts = retryAttempts; return this; }
        public Builder active(boolean active) { this.active = active; return this; }

        public Route build() { return new Route(this); }
    }
}

