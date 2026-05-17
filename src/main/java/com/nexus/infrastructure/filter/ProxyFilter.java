package com.nexus.infrastructure.filter;

import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;
import com.nexus.infrastructure.routing.HttpProxyClient;

@Component
public class ProxyFilter implements GatewayFilter {

    private final HttpProxyClient httpProxyClient;

    public ProxyFilter(HttpProxyClient httpProxyClient) {
        this.httpProxyClient = httpProxyClient;
    }

    @Override
    public int getOrder() { return 60; }

    @Override
    public void doFilter(GatewayContext ctx, FilterChain chain) throws Exception {
        String targetUrl = ctx.getTargetUrl();

        try {
            httpProxyClient.forward(ctx.getRequest(), ctx.getResponse(), targetUrl);
            ctx.setResponseStatus(ctx.getResponse().getStatus());
        } catch (Exception e) {
            ctx.getResponse().sendError(502, "Bad Gateway: upstream unreachable");
            ctx.setResponseStatus(502);
            return;
        }

        // No chain.proceed() — ProxyFilter is the terminal filter in the pre-phase.
        // Post-phase filters (MetricsFilter, RequestLoggingFilter) run after this returns.
        chain.proceed(ctx);
    }
}
