package com.nexus.infrastructure.filter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;

@Component
public class CorrelationIdFilter implements GatewayFilter {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";

    @Override
    public int getOrder() { return 1; }

    @Override
    public void doFilter(GatewayContext ctx, FilterChain chain) throws Exception {
        String correlationId = ctx.getRequest().getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        ctx.setCorrelationId(correlationId);
        ctx.getResponse().setHeader(CORRELATION_HEADER, correlationId);
        chain.proceed(ctx);
    }
}
