package com.nexus.infrastructure.persistence;

import java.util.UUID;

import com.nexus.domain.model.enums.AuthType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "routes")
public class RouteEntity {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "path_pattern", nullable = false)
    private String pathPattern;

    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    @Column(nullable = false, length = 10)
    private String method;

    @Column(name = "strip_prefix")
    private boolean stripPrefix;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    private AuthType authType;

    @Column(name = "retry_attempts", nullable = false)
    private int retryAttempts;

    @Column(nullable = false)
    private boolean active;

    protected RouteEntity() {}

    public RouteEntity(UUID id, UUID tenantId, String pathPattern, String targetUrl,
                       String method, boolean stripPrefix, AuthType authType,
                       int retryAttempts, boolean active) {
        this.id = id;
        this.tenantId = tenantId;
        this.pathPattern = pathPattern;
        this.targetUrl = targetUrl;
        this.method = method;
        this.stripPrefix = stripPrefix;
        this.authType = authType;
        this.retryAttempts = retryAttempts;
        this.active = active;
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
}
