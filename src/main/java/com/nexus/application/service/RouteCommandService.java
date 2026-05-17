package com.nexus.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexus.api.dto.CreateRouteRequest;
import com.nexus.api.dto.RouteResponse;
import com.nexus.domain.model.Route;
import com.nexus.domain.repository.IRouteRepository;
import com.nexus.infrastructure.messaging.KafkaRouteEventProducer;
import com.nexus.infrastructure.messaging.RouteUpdatedEvent;
import com.nexus.shared.exception.RouteNotFoundException;

@Service
public class RouteCommandService {

    private final IRouteRepository routeRepository;
    private final KafkaRouteEventProducer eventProducer;

    public RouteCommandService(IRouteRepository routeRepository, KafkaRouteEventProducer eventProducer) {
        this.routeRepository = routeRepository;
        this.eventProducer = eventProducer;
    }

    public RouteResponse createRoute(UUID tenantId, CreateRouteRequest request) {
        Route route = Route.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .pathPattern(request.getPathPattern())
                .targetUrl(request.getTargetUrl())
                .method(request.getMethod())
                .stripPrefix(request.isStripPrefix())
                .authType(request.getAuthType())
                .retryAttempts(request.getRetryAttempts())
                .active(true)
                .build();

        Route saved = routeRepository.save(route);
        eventProducer.publish(new RouteUpdatedEvent(saved.getId(), tenantId, "CREATED"));
        return toResponse(saved);
    }

    public RouteResponse getRoute(UUID routeId) {
        return toResponse(routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId)));
    }

    public List<RouteResponse> listRoutes(UUID tenantId) {
        return routeRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteRoute(UUID tenantId, UUID routeId) {
        routeRepository.deleteById(routeId);
        eventProducer.publish(new RouteUpdatedEvent(routeId, tenantId, "DELETED"));
    }

    private RouteResponse toResponse(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .tenantId(route.getTenantId())
                .pathPattern(route.getPathPattern())
                .targetUrl(route.getTargetUrl())
                .method(route.getMethod())
                .stripPrefix(route.isStripPrefix())
                .authType(route.getAuthType())
                .retryAttempts(route.getRetryAttempts())
                .active(route.isActive())
                .build();
    }
}
