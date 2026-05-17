package com.nexus.domain.auth;

import com.nexus.domain.filter.GatewayContext;
import com.nexus.shared.exception.AuthenticationException;

public interface AuthStrategy {
    void authenticate(GatewayContext ctx) throws AuthenticationException;
}
