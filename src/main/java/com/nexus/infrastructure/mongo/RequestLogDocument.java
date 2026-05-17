package com.nexus.infrastructure.mongo;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "request_logs")
class RequestLogDocument {

    @Id
    private String id;

    @Indexed
    private UUID tenantId;

    private UUID routeId;
    private String method;
    private String path;
    private int statusCode;
    private long latencyMs;
    private long timestamp;

    protected RequestLogDocument() {}

    RequestLogDocument(UUID tenantId, UUID routeId, String method, String path,
                       int statusCode, long latencyMs, long timestamp) {
        this.tenantId = tenantId;
        this.routeId = routeId;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.latencyMs = latencyMs;
        this.timestamp = timestamp;
    }

    public String getId()      { return id; }
    public UUID getTenantId()  { return tenantId; }
    public UUID getRouteId()   { return routeId; }
    public String getMethod()  { return method; }
    public String getPath()    { return path; }
    public int getStatusCode() { return statusCode; }
    public long getLatencyMs() { return latencyMs; }
    public long getTimestamp() { return timestamp; }
}
