package com.nexus.shared.exception;

public class AuthenticationException extends RuntimeException {

    private final int statusCode;

    public AuthenticationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() { return statusCode; }
}
