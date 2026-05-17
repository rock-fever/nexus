package com.nexus.infrastructure.filter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;
import com.nexus.domain.model.Tenant;
import com.nexus.domain.repository.ITenantRepository;
import com.nexus.infrastructure.ratelimit.RateLimitStrategyFactory;
import com.nexus.shared.exception.RateLimitException;
import com.nexus.shared.exception.TenantNotFoundException;

@Component
public class RateLimitFilter implements GatewayFilter {

    private final ITenantRepository tenantRepository;
    private final RateLimitStrategyFactory rateLimitStrategyFactory;

    public RateLimitFilter(ITenantRepository tenantRepository, RateLimitStrategyFactory rateLimitStrategyFactory) {
        this.tenantRepository = tenantRepository;
        this.rateLimitStrategyFactory = rateLimitStrategyFactory;
    }

    @Override
    public int getOrder() { return 20; }

    @Override
    public void doFilter(GatewayContext ctx, FilterChain chain) throws Exception {
        UUID tenantId = ctx.getTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

        try {
            rateLimitStrategyFactory.getStrategy(tenant.getPlan()).checkRateLimit(tenantId);
        } catch (RateLimitException e) {
            ctx.getResponse().sendError(429, e.getMessage());
            return;
        }

        chain.proceed(ctx);
    }
}
