package com.nexus.infrastructure.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.nexus.domain.auth.AuthStrategy;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.shared.exception.AuthenticationException;
import com.nexus.shared.security.AdminJwtUtil;

@Component
public class JwtAuthStrategy implements AuthStrategy {

    private final AdminJwtUtil adminJwtUtil;

    public JwtAuthStrategy(AdminJwtUtil adminJwtUtil) {
        this.adminJwtUtil = adminJwtUtil;
    }

    @Override
    public void authenticate(GatewayContext ctx) throws AuthenticationException {
        String header = ctx.getRequest().getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new AuthenticationException(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid Authorization header");
        }

        String token = header.substring(7);

        if (!adminJwtUtil.isValid(token)) {
            throw new AuthenticationException(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired JWT");
        }

        ctx.setAuthenticated(true);
    }
}
