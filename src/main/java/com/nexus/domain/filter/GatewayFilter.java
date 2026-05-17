package com.nexus.domain.filter;

public interface GatewayFilter {
    void doFilter(GatewayContext ctx, FilterChain chain) throws Exception;
    int getOrder();
    default boolean shouldSkip(GatewayContext ctx) { return false; }
}
