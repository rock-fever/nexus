package com.nexus.domain.filter;

public interface FilterChain {
    void proceed(GatewayContext ctx) throws Exception;
}
