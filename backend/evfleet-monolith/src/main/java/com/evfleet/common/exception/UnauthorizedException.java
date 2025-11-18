package com.evfleet.common.exception;

/**
 * Exception thrown when authentication is required but not provided or invalid
 * Results in HTTP 401 Unauthorized
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
