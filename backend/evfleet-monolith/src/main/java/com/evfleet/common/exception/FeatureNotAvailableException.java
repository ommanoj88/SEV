package com.evfleet.common.exception;

/**
 * Exception thrown when a feature is not available for a specific vehicle type or tier
 * Results in HTTP 403 Forbidden
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
public class FeatureNotAvailableException extends RuntimeException {

    public FeatureNotAvailableException(String message) {
        super(message);
    }

    public FeatureNotAvailableException(String feature, String reason) {
        super(String.format("Feature '%s' is not available: %s", feature, reason));
    }

    public FeatureNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
