package com.nexus.infrastructure.auth;

import org.springframework.stereotype.Component;

import com.nexus.domain.auth.AuthStrategy;
import com.nexus.domain.filter.GatewayContext;

@Component
public class PassThroughAuthStrategy implements AuthStrategy {

    @Override
    public void authenticate(GatewayContext ctx) {
        ctx.setAuthenticated(true);
    }
}
