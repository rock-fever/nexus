package com.nexus.infrastructure.resilience;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;

@Component
public class RouteCircuitBreakerRegistry {

    private final int failureRateThreshold;
    private final long waitDurationOpenMs;
    private final int slidingWindowSize;

    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public RouteCircuitBreakerRegistry(
            @Value("${nexus.resilience.circuit-breaker.failure-rate-threshold}") int failureRateThreshold,
            @Value("${nexus.resilience.circuit-breaker.wait-duration-open-ms}") long waitDurationOpenMs,
            @Value("${nexus.resilience.circuit-breaker.sliding-window-size}") int slidingWindowSize) {
        this.failureRateThreshold = failureRateThreshold;
        this.waitDurationOpenMs = waitDurationOpenMs;
        this.slidingWindowSize = slidingWindowSize;
    }

    public CircuitBreaker getOrCreate(String routeId) {
        return circuitBreakers.computeIfAbsent(routeId, id -> {
            CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                    .failureRateThreshold(failureRateThreshold)
                    .waitDurationInOpenState(Duration.ofMillis(waitDurationOpenMs))
                    .slidingWindowSize(slidingWindowSize)
                    .slidingWindowType(SlidingWindowType.COUNT_BASED)
                    .build();
            return CircuitBreaker.of(id, config);
        });
    }
}
