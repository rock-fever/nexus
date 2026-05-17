package com.nexus.infrastructure.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.nexus.infrastructure.cache.CachedRouteRepository;

@Component
public class RouteProjectionConsumer {

    private final CachedRouteRepository cachedRouteRepository;

    public RouteProjectionConsumer(CachedRouteRepository cachedRouteRepository) {
        this.cachedRouteRepository = cachedRouteRepository;
    }

    @KafkaListener(topics = "route.updated", groupId = "nexus-group")
    public void onRouteUpdated(RouteUpdatedEvent event) {
        cachedRouteRepository.evictByTenantId(event.tenantId());
    }
}
