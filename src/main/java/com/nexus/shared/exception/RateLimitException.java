package com.nexus.shared.exception;

public class RateLimitException extends RuntimeException{
    public RateLimitException(int limit) {
        super("Rate limit of " + limit + " req/min is exhausted. Please retry after sometime.");
    }
}
