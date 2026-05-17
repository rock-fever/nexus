package com.nexus.infrastructure.ratelimit;

import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class EnterprisePlanRateLimiter extends AbstractTenantRateLimiter {

    protected EnterprisePlanRateLimiter(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    protected int getLimit() {
        return Integer.MAX_VALUE;
    }

    // Enterprise has no cap — skip the Redis round-trip entirely.
    @Override
    public void checkRateLimit(UUID tenantId) {}
}
