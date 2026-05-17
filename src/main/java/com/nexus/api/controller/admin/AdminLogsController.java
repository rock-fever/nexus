package com.nexus.api.controller.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.api.dto.RequestLogResponse;
import com.nexus.domain.repository.IRequestLogRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Logs", description = "Request log history for a tenant")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/tenants/{tenantId}/logs")
public class AdminLogsController {

    private final IRequestLogRepository requestLogRepository;

    public AdminLogsController(IRequestLogRepository requestLogRepository) {
        this.requestLogRepository = requestLogRepository;
    }

    @Operation(summary = "Get request logs", description = "Returns recent gateway request logs for a tenant, optionally filtered by HTTP status code")
    @ApiResponse(responseCode = "200", description = "Logs returned")
    @GetMapping
    public ResponseEntity<List<RequestLogResponse>> getLogs(
            @PathVariable UUID tenantId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Integer status) {

        List<RequestLogResponse> logs = requestLogRepository
                .findByTenantId(tenantId, limit, status)
                .stream()
                .map(log -> new RequestLogResponse(
                        log.getTenantId(), log.getRouteId(), log.getMethod(), log.getPath(),
                        log.getStatusCode(), log.getLatencyMs(), log.getTimestamp()))
                .toList();

        return ResponseEntity.ok(logs);
    }
}
