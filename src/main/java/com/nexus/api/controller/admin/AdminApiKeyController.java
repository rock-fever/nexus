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

import com.nexus.api.dto.ApiKeyResponse;
import com.nexus.api.dto.CreateApiKeyRequest;
import com.nexus.application.service.ApiKeyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "API Keys", description = "API key management for tenant authentication")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/admin/tenants/{tenantId}/keys")
public class AdminApiKeyController {

    private final ApiKeyService apiKeyService;

    public AdminApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Operation(summary = "Create API key", description = "Issues a new API key for the tenant; the raw key is only returned once")
    @ApiResponse(responseCode = "201", description = "API key created")
    @PostMapping
    public ResponseEntity<ApiKeyResponse> createApiKey(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createApiKey(tenantId, request));
    }

    @Operation(summary = "List API keys for tenant")
    @ApiResponse(responseCode = "200", description = "Keys returned (rawKey is null in list responses)")
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(apiKeyService.listApiKeys(tenantId));
    }

    @Operation(summary = "Revoke API key", description = "Deactivates the key by its UUID; use UUID not hash because hashes contain '/' which breaks URL routing")
    @ApiResponse(responseCode = "204", description = "Key revoked")
    @ApiResponse(responseCode = "404", description = "Key not found")
    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revokeApiKey(
            @PathVariable UUID tenantId,
            @PathVariable UUID keyId) {
        apiKeyService.revokeApiKey(keyId);
        return ResponseEntity.noContent().build();
    }
}
