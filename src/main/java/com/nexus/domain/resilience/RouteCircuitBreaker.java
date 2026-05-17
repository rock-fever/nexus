package com.nexus.domain.resilience;

import java.util.UUID;

public interface RouteCircuitBreaker {
    void recordSuccess(UUID routeId);
    void recordFailure(UUID routeId);
    CircuitBreakerState getState(UUID routeId);
}
