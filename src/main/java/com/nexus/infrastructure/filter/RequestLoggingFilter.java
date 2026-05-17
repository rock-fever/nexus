package com.nexus.infrastructure.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;

@Component
public class RequestLoggingFilter implements GatewayFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public int getOrder() { return 100; }

    @Override
    public void doFilter(GatewayContext ctx, FilterChain chain) throws Exception {
        chain.proceed(ctx);

        long latencyMs = System.currentTimeMillis() - ctx.getStartTimeMs();

        log.info("tenantId={} correlationId={} method={} path={} status={} latencyMs={}",
                ctx.getTenantId(),
                ctx.getCorrelationId(),
                ctx.getRequest().getMethod(),
                ctx.getRequest().getRequestURI(),
                ctx.getResponseStatus(),
                latencyMs);
    }
}
