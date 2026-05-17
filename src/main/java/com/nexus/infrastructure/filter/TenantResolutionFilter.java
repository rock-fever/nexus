package com.nexus.infrastructure.filter;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;
import com.nexus.domain.model.Tenant;
import com.nexus.domain.model.enums.TenantStatus;
import com.nexus.domain.repository.ITenantRepository;

@Component
public class TenantResolutionFilter implements GatewayFilter {

    private static final String TENANT_SLUG_HEADER = "X-Tenant-Slug";

    private final ITenantRepository tenantRepository;

    public TenantResolutionFilter(ITenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public int getOrder() { return 2; }

    @Override
    public void doFilter(GatewayContext ctx, FilterChain chain) throws Exception {
        String slug = ctx.getRequest().getHeader(TENANT_SLUG_HEADER);

        if (slug == null || slug.isBlank()) {
            ctx.getResponse().sendError(HttpStatus.BAD_REQUEST.value(), "Missing X-Tenant-Slug header");
            return;
        }

        Tenant tenant = tenantRepository.findBySlug(slug).orElse(null);

        if (tenant == null) {
            ctx.getResponse().sendError(HttpStatus.NOT_FOUND.value(), "Tenant not found: " + slug);
            return;
        }

        if (tenant.getTenantStatus() != TenantStatus.ACTIVE) {
            ctx.getResponse().sendError(HttpStatus.FORBIDDEN.value(), "Tenant is suspended");
            return;
        }

        ctx.setTenantId(tenant.getId());
        chain.proceed(ctx);
    }
}
