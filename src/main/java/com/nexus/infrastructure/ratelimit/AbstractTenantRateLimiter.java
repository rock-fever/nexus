package com.nexus.infrastructure.ratelimit;

import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import com.nexus.shared.exception.RateLimitException;

public abstract class AbstractTenantRateLimiter {

    // Fixed 60-second window; key encodes tenantId + window bucket so counters reset naturally.
    private static final long WINDOW_MS = 60_000L;

    // Lua script: atomically increment a per-window counter and set TTL on first touch.
    // Returns 1 if the request is allowed, 0 if the limit is exceeded.
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
        RATE_LIMIT_SCRIPT.setScriptText("""
            local key    = KEYS[1]
            local limit  = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local count  = redis.call('INCR', key)
            if count == 1 then
                redis.call('PEXPIRE', key, window)
            end
            if count > limit then
                return 0
            end
            return 1
            """);
    }

    private final RedisTemplate<String, String> redisTemplate;

    protected AbstractTenantRateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Subclasses supply the per-minute request cap for their plan.
    protected abstract int getLimit();

    @SuppressWarnings("null")
    public void checkRateLimit(UUID tenantId) {
        long windowBucket = System.currentTimeMillis() / WINDOW_MS;
        String key = "ratelimit:" + tenantId + ":" + windowBucket;

        Long result = redisTemplate.execute(
            RATE_LIMIT_SCRIPT,
            List.of(key),
            String.valueOf(getLimit()),
            String.valueOf(WINDOW_MS)
        );

        if (result == null || result == 0L) {
            throw new RateLimitException(getLimit());
        }
    }
}
