package com.nexus.infrastructure.filter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.FilterRegistry;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;

@Component
public class FilterChainExecutor {

    private final FilterRegistry filterRegistry;

    public FilterChainExecutor(FilterRegistry filterRegistry) {
        this.filterRegistry = filterRegistry;
    }

    public void execute(GatewayContext ctx) throws Exception {
        List<GatewayFilter> filters = filterRegistry.getOrderedFilters();
        buildChain(filters, 0).proceed(ctx);
    }

    private FilterChain buildChain(List<GatewayFilter> filters, int index) {
        if (index >= filters.size()) {
            return ctx -> {};
        }
        GatewayFilter current = filters.get(index);
        FilterChain next = buildChain(filters, index + 1);

        return ctx -> {
            if (current.shouldSkip(ctx)) {
                next.proceed(ctx);
            } else {
                current.doFilter(ctx, next);
            }
        };
    }
}
