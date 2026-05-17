package com.nexus.application.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexus.api.dto.ApiKeyResponse;
import com.nexus.api.dto.CreateApiKeyRequest;
import com.nexus.domain.model.ApiKey;
import com.nexus.domain.repository.IApiKeyRepository;

@Service
public class ApiKeyService {

    private final IApiKeyRepository apiKeyRepository;

    public ApiKeyService(IApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public ApiKeyResponse createApiKey(UUID tenantId, CreateApiKeyRequest request) {
        String rawKey = UUID.randomUUID().toString().replace("-", "");
        String keyHash = hash(rawKey);

        ApiKey apiKey = ApiKey.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .name(request.getName())
                .keyHash(keyHash)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        ApiKey saved = apiKeyRepository.save(apiKey);

        return toResponse(saved, rawKey);
    }

    public List<ApiKeyResponse> listApiKeys(UUID tenantId) {
        return apiKeyRepository.findByTenantId(tenantId).stream()
                .map(k -> toResponse(k, null))
                .toList();
    }

    public void revokeApiKey(UUID id) {
        apiKeyRepository.revokeById(id);
    }

    private ApiKeyResponse toResponse(ApiKey apiKey, String rawKey) {
        return ApiKeyResponse.builder()
                .id(apiKey.getId())
                .tenantId(apiKey.getTenantId())
                .name(apiKey.getName())
                .active(apiKey.isActive())
                .createdAt(apiKey.getCreatedAt())
                .rawKey(rawKey)
                .build();
    }

    private String hash(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawKey.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
