package com.nexus.api.controller.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.api.dto.TenantConfigEventResponse;
import com.nexus.infrastructure.mongo.TenantConfigEventStore;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "History", description = "Tenant config event sourcing history")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/tenants/{tenantId}/history")
public class AdminTenantHistoryController {

    private final TenantConfigEventStore eventStore;

    public AdminTenantHistoryController(TenantConfigEventStore eventStore) {
        this.eventStore = eventStore;
    }

    @GetMapping
    public ResponseEntity<List<TenantConfigEventResponse>> getHistory(@PathVariable UUID tenantId) {
        List<TenantConfigEventResponse> events = eventStore.findByTenantId(tenantId)
                .stream()
                .map(e -> new TenantConfigEventResponse(
                        e.tenantId(), e.eventType(), e.payload(), e.timestamp()))
                .toList();
        return ResponseEntity.ok(events);
    }
}
