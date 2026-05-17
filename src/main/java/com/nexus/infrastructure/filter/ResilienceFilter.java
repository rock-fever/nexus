package com.nexus.infrastructure.filter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;
import com.nexus.domain.model.Tenant;
import com.nexus.domain.resilience.CircuitBreakerState;
import com.nexus.domain.resilience.RouteCircuitBreaker;
import com.nexus.domain.repository.ITenantRepository;
import com.nexus.infrastructure.resilience.TenantBulkhead;
import com.nexus.shared.exception.TenantNotFoundException;

@Component
public class ResilienceFilter implements GatewayFilter {

    private final RouteCircuitBreaker circuitBreaker;
    private final TenantBulkhead bulkhead;
    private final ITenantRepository tenantRepository;

    public ResilienceFilter(RouteCircuitBreaker circuitBreaker,
                            TenantBulkhead bulkhead,
                            ITenantRepository tenantRepository) {
        this.circuitBreaker = circuitBreaker;
        this.bulkhead = bulkhead;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public int getOrder() { return 45; }

    @Override
    public void doFilter(GatewayContext ctx, FilterChain chain) throws Exception {
        UUID routeId = ctx.getResolvedRouteId();
        UUID tenantId = ctx.getTenantId();

        // Circuit breaker check — reject immediately if OPEN, no upstream call made.
        CircuitBreakerState state = circuitBreaker.getState(routeId);
        if (state == CircuitBreakerState.OPEN) {
            ctx.getResponse().sendError(503, "Service unavailable: circuit open for route " + routeId);
            return;
        }

        // Bulkhead check — reject if this tenant has too many concurrent requests in flight.
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        if (!bulkhead.tryAcquire(tenantId, tenant.getPlan())) {
            ctx.getResponse().sendError(503, "Too many concurrent requests");
            return;
        }

        try {
            chain.proceed(ctx);

            // Post-phase: record outcome. 5xx = upstream failure, anything else = success.
            if (ctx.getResponseStatus() >= 500) {
                circuitBreaker.recordFailure(routeId);
            } else {
                circuitBreaker.recordSuccess(routeId);
            }
        } finally {
            // Always release the bulkhead permit — even if a downstream filter throws.
            bulkhead.release(tenantId);
        }
    }
}
