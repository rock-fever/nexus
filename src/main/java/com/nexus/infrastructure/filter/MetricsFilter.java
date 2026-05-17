package com.nexus.infrastructure.filter;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;
import com.nexus.infrastructure.messaging.KafkaRequestLogProducer;
import com.nexus.infrastructure.messaging.RequestLoggedEvent;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Component
public class MetricsFilter implements GatewayFilter {

    private final MeterRegistry meterRegistry;
    private final KafkaRequestLogProducer logProducer;

    public MetricsFilter(MeterRegistry meterRegistry, KafkaRequestLogProducer logProducer) {
        this.meterRegistry = meterRegistry;
        this.logProducer = logProducer;
    }

    @Override
    public int getOrder() { return 90; }

    @Override
    public void doFilter(GatewayContext ctx, FilterChain chain) throws Exception {
        chain.proceed(ctx);

        // Post-phase: response has been written by ProxyFilter, status is set.
        int status = ctx.getResponseStatus();
        long latencyMs = System.currentTimeMillis() - ctx.getStartTimeMs();
        String tenantId = ctx.getTenantId() != null ? ctx.getTenantId().toString() : "unknown";
        String routeId  = ctx.getResolvedRouteId() != null ? ctx.getResolvedRouteId().toString() : "unknown";

        Counter.builder("gateway.requests.total")
                .tag("tenantId", tenantId)
                .tag("routeId", routeId)
                .tag("status", statusBucket(status))
                .register(meterRegistry)
                .increment();

        Timer.builder("gateway.request.duration")
                .tag("tenantId", tenantId)
                .register(meterRegistry)
                .record(latencyMs, TimeUnit.MILLISECONDS);

        // Only publish a log event when we have a fully resolved request.
        if (ctx.getTenantId() != null && ctx.getResolvedRouteId() != null) {
            logProducer.publish(new RequestLoggedEvent(
                    ctx.getTenantId(),
                    ctx.getResolvedRouteId(),
                    ctx.getRequest().getMethod(),
                    ctx.getRequest().getRequestURI(),
                    status,
                    latencyMs,
                    System.currentTimeMillis()));
        }
    }

    private String statusBucket(int status) {
        if (status >= 200 && status < 300) return "2xx";
        if (status >= 400 && status < 500) return "4xx";
        if (status >= 500) return "5xx";
        return "other";
    }
}
