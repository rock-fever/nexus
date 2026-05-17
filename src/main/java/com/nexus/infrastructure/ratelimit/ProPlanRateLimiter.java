package com.nexus.infrastructure.ratelimit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProPlanRateLimiter extends AbstractTenantRateLimiter {

    protected ProPlanRateLimiter(RedisTemplate<String, String> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    protected int getLimit() {
        return 600;
    }
}
