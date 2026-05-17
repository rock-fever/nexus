package com.nexus.shared.exception;

import java.util.UUID;

public class CircuitOpenException extends RuntimeException {

    public CircuitOpenException(UUID routeId) {
        super("Circuit breaker is OPEN for route: " + routeId);
    }
}
