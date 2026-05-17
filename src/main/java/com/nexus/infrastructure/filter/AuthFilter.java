package com.nexus.infrastructure.filter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import com.nexus.application.service.RouteQueryService;
import com.nexus.domain.auth.AuthStrategy;
import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;
import com.nexus.domain.model.Route;
import com.nexus.infrastructure.auth.AuthStrategyFactory;
import com.nexus.shared.exception.AuthenticationException;
import com.nexus.shared.exception.RouteNotFoundException;

@Component
public class AuthFilter implements GatewayFilter {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final RouteQueryService routeQueryService;
    private final AuthStrategyFactory authStrategyFactory;

    public AuthFilter(RouteQueryService routeQueryService, AuthStrategyFactory authStrategyFactory) {
        this.routeQueryService = routeQueryService;
        this.authStrategyFactory = authStrategyFactory;
    }

    @Override
    public int getOrder() { return 10; }

    @Override
    @SuppressWarnings("null")
    public void doFilter(GatewayContext ctx, FilterChain chain) throws Exception {
        String requestPath = ctx.getRequest().getRequestURI();
        String requestMethod = ctx.getRequest().getMethod();

        List<Route> routes = routeQueryService.findRoutesForTenant(ctx.getTenantId());

        Optional<Route> match = routes.stream()
                .filter(Route::isActive)
                .filter(r -> pathMatcher.match(r.getPathPattern(), requestPath))
                .filter(r -> r.getMethod().equalsIgnoreCase("ANY") || r.getMethod().equalsIgnoreCase(requestMethod))
                .findFirst();

        Route matched;
        try {
            matched = match.orElseThrow(() ->
                    new RouteNotFoundException("No matching route for: " + requestMethod + " " + requestPath));
        } catch (RouteNotFoundException e) {
            ctx.getResponse().sendError(404, e.getMessage());
            return;
        }

        ctx.setResolvedRouteId(matched.getId());
        ctx.setMatchedRoute(matched);
        ctx.setAttribute("resolvedRoute", matched);

        AuthStrategy authStrategy = authStrategyFactory.getStrategy(matched.getAuthType());

        try {
            authStrategy.authenticate(ctx);
        } catch (AuthenticationException e) {
            ctx.getResponse().sendError(e.getStatusCode(), e.getMessage());
            return;
        }

        chain.proceed(ctx);
    }
}
