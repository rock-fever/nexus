package com.nexus.infrastructure.auth;

import org.springframework.stereotype.Component;

import com.nexus.domain.auth.AuthStrategy;
import com.nexus.domain.model.enums.AuthType;

@Component
public class AuthStrategyFactory {

    private final ApiKeyAuthStrategy apiKeyAuthStrategy;
    private final JwtAuthStrategy jwtAuthStrategy;
    private final PassThroughAuthStrategy passThroughAuthStrategy;

    public AuthStrategyFactory(
        ApiKeyAuthStrategy apiKeyAuthStrategy, 
        JwtAuthStrategy jwtAuthStrategy, 
        PassThroughAuthStrategy passThroughAuthStrategy) {
            this.apiKeyAuthStrategy = apiKeyAuthStrategy;
            this.jwtAuthStrategy = jwtAuthStrategy;
            this.passThroughAuthStrategy = passThroughAuthStrategy;
    }

    public AuthStrategy getStrategy(AuthType authType) {
        return switch (authType) {
            case API_KEY -> apiKeyAuthStrategy;
            case JWT -> jwtAuthStrategy;
            case NONE -> passThroughAuthStrategy;
        };
    }
}
