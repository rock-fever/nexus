package com.nexus.infrastructure.ratelimit;

import org.springframework.stereotype.Component;

import com.nexus.domain.model.enums.Plan;

@Component
public class RateLimitStrategyFactory {

    private final FreePlanRateLimiter freePlanRateLimiter;
    private final ProPlanRateLimiter proPlanRateLimiter;
    private final EnterprisePlanRateLimiter enterprisePlanRateLimiter;

    public RateLimitStrategyFactory(
            FreePlanRateLimiter freePlanRateLimiter,
            ProPlanRateLimiter proPlanRateLimiter,
            EnterprisePlanRateLimiter enterprisePlanRateLimiter) {
        this.freePlanRateLimiter = freePlanRateLimiter;
        this.proPlanRateLimiter = proPlanRateLimiter;
        this.enterprisePlanRateLimiter = enterprisePlanRateLimiter;
    }

    public AbstractTenantRateLimiter getStrategy(Plan plan) {
        return switch (plan) {
            case FREE -> freePlanRateLimiter;
            case PRO -> proPlanRateLimiter;
            case ENTERPRISE -> enterprisePlanRateLimiter;
        };
    }
}
