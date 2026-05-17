package com.nexus.domain.model;

import java.util.UUID;

public class RequestLog {

    private final UUID tenantId;
    private final UUID routeId;
    private final String method;
    private final String path;
    private final int statusCode;
    private final long latencyMs;
    private final long timestamp;

    public RequestLog(UUID tenantId, UUID routeId, String method, String path,
                      int statusCode, long latencyMs, long timestamp) {
        this.tenantId = tenantId;
        this.routeId = routeId;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.latencyMs = latencyMs;
        this.timestamp = timestamp;
    }

    public UUID getTenantId()  { return tenantId; }
    public UUID getRouteId()   { return routeId; }
    public String getMethod()  { return method; }
    public String getPath()    { return path; }
    public int getStatusCode() { return statusCode; }
    public long getLatencyMs() { return latencyMs; }
    public long getTimestamp() { return timestamp; }
}
