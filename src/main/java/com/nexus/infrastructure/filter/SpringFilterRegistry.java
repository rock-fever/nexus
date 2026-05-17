package com.nexus.infrastructure.filter;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterRegistry;
import com.nexus.domain.filter.GatewayFilter;

@Component
public class SpringFilterRegistry implements FilterRegistry {

    private final List<GatewayFilter> filters;

    public SpringFilterRegistry(List<GatewayFilter> filters) {
        this.filters = filters.stream()
                .sorted(Comparator.comparingInt(GatewayFilter::getOrder))
                .toList();
    }

    @Override
    public List<GatewayFilter> getOrderedFilters() {
        return filters;
    }
}
