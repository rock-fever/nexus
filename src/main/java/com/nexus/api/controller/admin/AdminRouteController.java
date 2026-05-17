package com.nexus.api.controller.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.api.dto.CreateRouteRequest;
import com.nexus.api.dto.RouteResponse;
import com.nexus.application.service.RouteCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Routes", description = "Route configuration for a tenant")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/tenants/{tenantId}/routes")
public class AdminRouteController {

    private final RouteCommandService routeCommandService;

    public AdminRouteController(RouteCommandService routeCommandService) {
        this.routeCommandService = routeCommandService;
    }

    @Operation(summary = "Create route", description = "Adds a new route to the tenant's gateway configuration")
    @ApiResponse(responseCode = "201", description = "Route created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeCommandService.createRoute(tenantId, request));
    }

    @Operation(summary = "Get route by ID")
    @ApiResponse(responseCode = "200", description = "Route found")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponse> getRoute(
            @PathVariable UUID tenantId,
            @PathVariable UUID routeId) {
        return ResponseEntity.ok(routeCommandService.getRoute(routeId));
    }

    @Operation(summary = "List routes for tenant")
    @ApiResponse(responseCode = "200", description = "Routes returned")
    @GetMapping
    public ResponseEntity<List<RouteResponse>> listRoutes(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(routeCommandService.listRoutes(tenantId));
    }

    @Operation(summary = "Delete route", description = "Removes a route and invalidates its Redis projection")
    @ApiResponse(responseCode = "204", description = "Route deleted")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @DeleteMapping("/{routeId}")
    public ResponseEntity<Void> deleteRoute(
            @PathVariable UUID tenantId,
            @PathVariable UUID routeId) {
        routeCommandService.deleteRoute(tenantId, routeId);
        return ResponseEntity.noContent().build();
    }
}
