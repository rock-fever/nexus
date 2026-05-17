package com.nexus.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.nexus.AbstractIntegrationTest;
import com.nexus.api.dto.CreateRouteRequest;
import com.nexus.api.dto.CreateTenantRequest;
import com.nexus.api.dto.RouteResponse;
import com.nexus.domain.model.enums.AuthType;
import com.nexus.domain.model.enums.Plan;

class RouteCommandServiceTest extends AbstractIntegrationTest {

    @Autowired
    private RouteCommandService routeCommandService;

    @Autowired
    private TenantService tenantService;

    @Test
    void createRoute_persistsAndReturnsResponse() {
        UUID tenantId = createTenant();

        CreateRouteRequest request = CreateRouteRequest.builder()
                .pathPattern("/api/v1/**")
                .targetUrl("http://backend:8080")
                .method("GET")
                .stripPrefix(true)
                .authType(AuthType.API_KEY)
                .retryAttempts(2)
                .build();

        RouteResponse response = routeCommandService.createRoute(tenantId, request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getPathPattern()).isEqualTo("/api/v1/**");
        assertThat(response.getTargetUrl()).isEqualTo("http://backend:8080");
        assertThat(response.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    void listRoutes_returnsAllRoutesForTenant() {
        UUID tenantId = createTenant();

        routeCommandService.createRoute(tenantId, routeRequest("/route/one"));
        routeCommandService.createRoute(tenantId, routeRequest("/route/two"));

        List<RouteResponse> routes = routeCommandService.listRoutes(tenantId);

        assertThat(routes).hasSizeGreaterThanOrEqualTo(2);
        assertThat(routes).allMatch(r -> r.getTenantId().equals(tenantId));
    }

    @Test
    void deleteRoute_removesRoute() {
        UUID tenantId = createTenant();
        RouteResponse created = routeCommandService.createRoute(tenantId, routeRequest("/to/delete"));

        routeCommandService.deleteRoute(tenantId, created.getId());

        List<RouteResponse> remaining = routeCommandService.listRoutes(tenantId);
        assertThat(remaining).noneMatch(r -> r.getId().equals(created.getId()));
    }

    private UUID createTenant() {
        return tenantService.createTenant(CreateTenantRequest.builder()
                .name("Test")
                .slug("route-test-" + UUID.randomUUID())
                .plan(Plan.PRO)
                .build()).getId();
    }

    private CreateRouteRequest routeRequest(String path) {
        return CreateRouteRequest.builder()
                .pathPattern(path)
                .targetUrl("http://backend:8080")
                .method("ANY")
                .stripPrefix(false)
                .authType(AuthType.API_KEY)
                .retryAttempts(0)
                .build();
    }
}
