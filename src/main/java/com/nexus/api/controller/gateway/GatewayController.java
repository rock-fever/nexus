package com.nexus.api.controller.gateway;

import java.io.IOException;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nexus.domain.filter.GatewayContext;
import com.nexus.infrastructure.filter.FilterChainExecutor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 5)
public class GatewayController extends OncePerRequestFilter {

    private static final List<String> INTERNAL_PREFIXES = List.of(
        "/admin/", "/actuator", "/swagger-ui", "/v3/api-docs", "/error"
    );

    private final FilterChainExecutor executor;

    public GatewayController(FilterChainExecutor executor) {
        this.executor = executor;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        for (String prefix : INTERNAL_PREFIXES) {
            if (path.startsWith(prefix)) {
                chain.doFilter(request, response);
                return;
            }
        }
        try {
            GatewayContext ctx = new GatewayContext(request, response);
            executor.execute(ctx);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
