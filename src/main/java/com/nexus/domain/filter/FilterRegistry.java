package com.nexus.domain.filter;

import java.util.List;

public interface FilterRegistry {
    List<GatewayFilter> getOrderedFilters();
}
