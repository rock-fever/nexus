package com.nexus.domain.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.nexus.domain.model.Route;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GatewayContext {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private String correlationId;
    private UUID tenantId;
    private UUID resolvedRouteId;
    private Route matchedRoute;
    private boolean authenticated;
    private String targetUrl;
    private int responseStatus;
    private long startTimeMs;
    private final Map<String, Object> attributes = new HashMap<>();

    public GatewayContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.startTimeMs = System.currentTimeMillis();
    }

    public HttpServletRequest getRequest() { return request; }
    public HttpServletResponse getResponse() { return response; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public UUID getResolvedRouteId() { return resolvedRouteId; }
    public void setResolvedRouteId(UUID resolvedRouteId) { this.resolvedRouteId = resolvedRouteId; }

    public Route getMatchedRoute() { return matchedRoute; }
    public void setMatchedRoute(Route matchedRoute) { this.matchedRoute = matchedRoute; }

    public boolean isAuthenticated() { return authenticated; }
    public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    public int getResponseStatus() { return responseStatus; }
    public void setResponseStatus(int responseStatus) { this.responseStatus = responseStatus; }

    public long getStartTimeMs() { return startTimeMs; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttribute(String key, Object value) { attributes.put(key, value); }
    public Object getAttribute(String key) { return attributes.get(key); }
}
