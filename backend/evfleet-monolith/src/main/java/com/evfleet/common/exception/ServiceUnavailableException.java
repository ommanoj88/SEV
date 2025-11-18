package com.evfleet.common.exception;

/**
 * Exception thrown when a required service or resource is unavailable
 * Results in HTTP 503 Service Unavailable
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
