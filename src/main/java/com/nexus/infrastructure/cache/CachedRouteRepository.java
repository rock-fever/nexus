package com.nexus.infrastructure.cache;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.domain.model.Route;
import com.nexus.domain.repository.IRouteQueryRepository;

@Primary
@Repository
public class CachedRouteRepository implements IRouteQueryRepository {

    private static final String KEY_PREFIX = "routes:";
    private static final long TTL_SECONDS = 300;

    private static final TypeReference<List<RouteCacheEntry>> ENTRY_LIST_TYPE =
            new TypeReference<>() {};

    private final IRouteQueryRepository delegate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CachedRouteRepository(
            @Qualifier("jpaRouteQueryRepository") IRouteQueryRepository delegate,
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @SuppressWarnings("null")
    public List<Route> findByTenantId(UUID tenantId) {
        String key = KEY_PREFIX + tenantId;
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return toRoutes(objectMapper.readValue(cached, ENTRY_LIST_TYPE));
            }
        } catch (Exception ignored) {
            // cache miss on error — fall through to delegate
        }

        List<Route> routes = delegate.findByTenantId(tenantId);

        try {
            String json = objectMapper.writeValueAsString(toEntries(routes));
            redisTemplate.opsForValue().set(key, json, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // best-effort cache write
        }

        return routes;
    }

    // Called by RouteProjectionConsumer when a route.updated event arrives.
    @SuppressWarnings("null")
    public void evictByTenantId(UUID tenantId) {
        redisTemplate.delete(KEY_PREFIX + tenantId);
    }

    private List<RouteCacheEntry> toEntries(List<Route> routes) {
        return routes.stream()
                .map(r -> new RouteCacheEntry(
                        r.getId(), r.getTenantId(), r.getPathPattern(), r.getTargetUrl(),
                        r.getMethod(), r.isStripPrefix(), r.getAuthType(),
                        r.getRetryAttempts(), r.isActive()))
                .toList();
    }

    private List<Route> toRoutes(List<RouteCacheEntry> entries) {
        if (entries == null) return Collections.emptyList();
        return entries.stream()
                .map(e -> Route.builder()
                        .id(e.id())
                        .tenantId(e.tenantId())
                        .pathPattern(e.pathPattern())
                        .targetUrl(e.targetUrl())
                        .method(e.method())
                        .stripPrefix(e.stripPrefix())
                        .authType(e.authType())
                        .retryAttempts(e.retryAttempts())
                        .active(e.active())
                        .build())
                .toList();
    }
}
