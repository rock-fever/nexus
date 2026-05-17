package com.nexus.infrastructure.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.nexus.domain.auth.AuthStrategy;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.model.ApiKey;
import com.nexus.domain.repository.IApiKeyRepository;
import com.nexus.shared.exception.AuthenticationException;

@Component
public class ApiKeyAuthStrategy implements AuthStrategy {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final String CACHE_PREFIX = "apikey:";
    private static final long CACHE_TTL_MINUTES = 10;

    private final IApiKeyRepository apiKeyRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public ApiKeyAuthStrategy(IApiKeyRepository apiKeyRepository,
                               RedisTemplate<String, String> redisTemplate) {
        this.apiKeyRepository = apiKeyRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @SuppressWarnings("null")
    public void authenticate(GatewayContext ctx) throws AuthenticationException {
        String rawKey = ctx.getRequest().getHeader(API_KEY_HEADER);

        if (rawKey == null || rawKey.isBlank()) {
            throw new AuthenticationException(HttpStatus.UNAUTHORIZED.value(), "Missing X-Api-Key header");
        }

        String keyHash = hash(rawKey);
        String cacheKey = CACHE_PREFIX + keyHash;

        String cachedTenantId = redisTemplate.opsForValue().get(cacheKey);

        if (cachedTenantId != null) {
            validateTenant(UUID.fromString(cachedTenantId), ctx);
            ctx.setAuthenticated(true);
            return;
        }

        ApiKey apiKey = apiKeyRepository.findByKeyHash(keyHash)
                .filter(ApiKey::isActive)
                .orElseThrow(() -> new AuthenticationException(HttpStatus.UNAUTHORIZED.value(), "Invalid or revoked API key"));

        validateTenant(apiKey.getTenantId(), ctx);

        redisTemplate.opsForValue().set(cacheKey, apiKey.getTenantId().toString(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);

        ctx.setAuthenticated(true);
    }

    private void validateTenant(UUID keyTenantId, GatewayContext ctx) throws AuthenticationException {
        if (!keyTenantId.equals(ctx.getTenantId())) {
            throw new AuthenticationException(HttpStatus.UNAUTHORIZED.value(), "API key does not belong to this tenant");
        }
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
