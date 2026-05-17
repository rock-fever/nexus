package com.nexus.infrastructure.filter;

import org.springframework.stereotype.Component;

import com.nexus.domain.filter.FilterChain;
import com.nexus.domain.filter.GatewayContext;
import com.nexus.domain.filter.GatewayFilter;
import com.nexus.domain.model.Route;

@Component
public class RoutingFilter implements GatewayFilter {

    @Override
    public int getOrder() { return 50; }

    @Override
    public void doFilter(GatewayContext ctx, FilterChain chain) throws Exception {
        Route route = ctx.getMatchedRoute();
        String requestUri = ctx.getRequest().getRequestURI();
        String queryString = ctx.getRequest().getQueryString();

        String path = route.isStripPrefix()
                ? stripPrefix(route.getPathPattern(), requestUri)
                : requestUri;

        String targetBase = route.getTargetUrl();
        if (targetBase.endsWith("/") && path.startsWith("/")) {
            targetBase = targetBase.substring(0, targetBase.length() - 1);
        }

        String targetUrl = targetBase + path;
        if (queryString != null && !queryString.isBlank()) {
            targetUrl += "?" + queryString;
        }

        ctx.setTargetUrl(targetUrl);
        chain.proceed(ctx);
    }

    // Removes the static prefix of the pattern from the request URI.
    // e.g. pattern="/api/**", uri="/api/users/1" → "/users/1"
    private String stripPrefix(String pattern, String requestUri) {
        int wildcardIdx = pattern.indexOf('*');
        if (wildcardIdx < 0) {
            return "";
        }
        String prefix = pattern.substring(0, wildcardIdx).replaceAll("/$", "");
        return requestUri.startsWith(prefix) ? requestUri.substring(prefix.length()) : requestUri;
    }
}
