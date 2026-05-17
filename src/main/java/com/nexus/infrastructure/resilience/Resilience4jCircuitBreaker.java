package com.nexus.infrastructure.resilience;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import com.nexus.domain.resilience.CircuitBreakerState;
import com.nexus.domain.resilience.RouteCircuitBreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import jakarta.annotation.PostConstruct;

@Component
public class Resilience4jCircuitBreaker implements RouteCircuitBreaker {

    private static final String KEY_PREFIX = "cb:state:";

    private final RouteCircuitBreakerRegistry registry;
    private final RedisTemplate<String, String> redisTemplate;

    public Resilience4jCircuitBreaker(RouteCircuitBreakerRegistry registry,
                                      RedisTemplate<String, String> redisTemplate) {
        this.registry = registry;
        this.redisTemplate = redisTemplate;
    }

    // On startup: scan Redis for saved circuit states and restore them into Resilience4j
    // so the in-memory state machine matches what was persisted before the restart.
    // The wait timer restarts from now (not from the original trip time) — acceptable trade-off.
    @PostConstruct
    public void restoreStateFromRedis() {
        try {
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                ScanOptions opts = ScanOptions.scanOptions()
                        .match(KEY_PREFIX + "*").count(100).build();
                try (Cursor<byte[]> cursor = connection.keyCommands().scan(opts)) {
                    cursor.forEachRemaining(keyBytes -> {
                        String key = new String(keyBytes, StandardCharsets.UTF_8);
                        String stateStr = redisTemplate.opsForValue().get(key);
                        if (stateStr == null) return;

                        String routeIdStr = key.substring(KEY_PREFIX.length());
                        try {
                            CircuitBreakerState savedState = CircuitBreakerState.valueOf(stateStr);
                            if (savedState == CircuitBreakerState.OPEN
                                    || savedState == CircuitBreakerState.HALF_OPEN) {
                                // transitionToForcedOpenState() works from any state,
                                // so a freshly-created (CLOSED) CB can be moved to OPEN immediately.
                                registry.getOrCreate(routeIdStr).transitionToForcedOpenState();
                            }
                        } catch (IllegalArgumentException ignored) {
                            // Malformed state value in Redis — skip
                        }
                    });
                } catch (Exception ignored) {}
                return null;
            });
        } catch (Exception ignored) {
            // Redis unavailable on startup — all circuits start fresh (CLOSED)
        }
    }

    @Override
    public void recordSuccess(UUID routeId) {
        CircuitBreaker cb = registry.getOrCreate(routeId.toString());
        cb.onSuccess(0, TimeUnit.NANOSECONDS);
        persistState(routeId, cb);
    }

    @Override
    public void recordFailure(UUID routeId) {
        CircuitBreaker cb = registry.getOrCreate(routeId.toString());
        cb.onError(0, TimeUnit.NANOSECONDS, new RuntimeException("upstream error"));
        persistState(routeId, cb);
    }

    @Override
    @SuppressWarnings("null")
    public CircuitBreakerState getState(UUID routeId) {
        // Redis is the fast path; falls back to in-memory if key is absent (new route).
        String cached = redisTemplate.opsForValue().get(KEY_PREFIX + routeId);
        if (cached != null) {
            return CircuitBreakerState.valueOf(cached);
        }
        return mapState(registry.getOrCreate(routeId.toString()).getState());
    }

    @SuppressWarnings("null")
    private void persistState(UUID routeId, CircuitBreaker cb) {
        redisTemplate.opsForValue().set(KEY_PREFIX + routeId, mapState(cb.getState()).name());
    }

    private CircuitBreakerState mapState(CircuitBreaker.State r4jState) {
        return switch (r4jState) {
            case OPEN, FORCED_OPEN -> CircuitBreakerState.OPEN;
            case HALF_OPEN -> CircuitBreakerState.HALF_OPEN;
            default -> CircuitBreakerState.CLOSED;
        };
    }
}
